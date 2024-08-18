/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.ophis.vanilla.internal.commands;

import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTException;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.blackthorne.core.Blackthorne;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXEXInclude;
import com.io7m.ophis.api.OClientAccessKeys;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.api.commands.OObjectData;
import com.io7m.ophis.vanilla.internal.OCanonicalRequest;
import com.io7m.ophis.vanilla.internal.OClient;
import com.io7m.ophis.vanilla.internal.OResourceRelative;
import com.io7m.ophis.vanilla.internal.OTimeFormatters;
import com.io7m.ophis.vanilla.internal.OUserAgent;
import com.io7m.ophis.vanilla.internal.xml.OXErrorParsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class OCmdAbstract<P, R>
{
  private static final Set<String> RESTRICTED_HEADERS =
    Set.of(
      "HOST",
      "CONTENT-LENGTH"
    );

  private final P parameters;
  private final OClient client;
  private final HashMap<String, String> attributes;
  private final OCanonicalRequest.Builder canonicalRequest;
  private final String timestampFull;
  private final String timestampSigner;
  private Optional<String> bucket;

  OCmdAbstract(
    final OClient inClient,
    final P inParameters)
  {
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.parameters =
      Objects.requireNonNull(inParameters, "parameters");
    this.attributes =
      new HashMap<>();
    this.canonicalRequest =
      OCanonicalRequest.builder();
    this.timestampFull =
      OTimeFormatters.amzDateFormatString();
    this.timestampSigner =
      OTimeFormatters.signerFormatString();
    this.bucket =
      Optional.empty();

    this.canonicalRequest.setHeader("x-amz-date", this.timestampFull);
  }

  private String hostString()
  {
    final var configuration =
      this.client.configuration();
    final var endpoint =
      configuration.endpoint();
    final var host =
      endpoint.getHost();
    final var port =
      endpoint.getPort();

    final var result = new StringBuilder();
    this.bucket.ifPresent(name -> {
      switch (configuration.bucketAccessStyle()) {
        case VIRTUALHOST_STYLE -> {
          result.append(name);
          result.append('.');
        }
        case PATH_STYLE -> {
          // Nothing required
        }
      }
    });
    result.append(host);

    if (port >= 0) {
      result.append(':');
      result.append(port);
    }
    return result.toString();
  }

  protected final OClient client()
  {
    return this.client;
  }

  protected final P parameters()
  {
    return this.parameters;
  }

  protected final HttpClient httpClient()
  {
    return this.client.httpClient();
  }

  protected final JXEHardenedSAXParsers saxParsers()
  {
    return this.client.saxParsers();
  }

  protected final void setAttribute(
    final String name,
    final String value)
  {
    this.attributes.put(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(value, "value")
    );
  }

  protected final void setHeader(
    final String name,
    final String value)
  {
    this.canonicalRequest.setHeader(name, value);
  }

  protected <T> T sendPUT(
    final OObjectData data,
    final OResourceRelative key,
    final Function<HttpHeaders, T> transform)
    throws OException
  {
    this.setMethod("PUT");
    this.canonicalRequest.setHeader("Host", this.hostString());

    /*
     * If we're using path-style buckets, then the resource name must be
     * set to the bucket name and the key (such as "/bucket-0/key"). Otherwise,
     * the resource name is just the key.
     */

    if (this.bucket.isPresent()) {
      switch (this.client.configuration().bucketAccessStyle()) {
        case VIRTUALHOST_STYLE -> {
          this.setResource(key);
        }
        case PATH_STYLE -> {
          final var elements = new ArrayList<String>();
          elements.add(this.bucket.get());
          elements.addAll(key.segments());
          this.setResource(new OResourceRelative(elements));
        }
      }
    }

    this.canonicalRequest.setHashedPayload(data.sha256());

    final var canonical =
      this.canonicalRequest.build();
    final var canonicalHash =
      canonical.hash();

    this.setAttribute("Resource", canonical.resource().toString());
    this.setAttribute("Method", canonical.httpVerb());
    this.setAttribute("Canonical Request Hash", canonicalHash);

    final var requestBuilder =
      this.createInitialSignedRequestBuilder(canonical, canonicalHash);

    final byte[] dataBytes;
    try {
      dataBytes = data.stream()
        .get()
        .readAllBytes();
    } catch (final IOException e) {
      throw new OException(
        e,
        "error-io",
        Map.copyOf(this.attributes),
        Optional.empty()
      );
    }

    final var request =
      requestBuilder.PUT(BodyPublishers.ofByteArray(dataBytes))
        .build();

    final var response =
      this.executeHTTPRequest(request);

    return transform.apply(response.headers());
  }

  private HttpRequest.Builder createInitialSignedRequestBuilder(
    final OCanonicalRequest canonical,
    final String canonicalHash)
    throws OException
  {
    final URI bucketEndpoint;
    try {
      bucketEndpoint = this.bucketEndpoint();
    } catch (final URISyntaxException e) {
      throw new OException(
        e,
        "error-uri-syntax",
        Map.copyOf(this.attributes),
        Optional.empty()
      );
    }

    final var uriSource =
      canonical.queryURI(bucketEndpoint);

    /*
     * See: "https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html"
     */

    final var stringToSign = new StringBuilder();
    stringToSign.append("AWS4-HMAC-SHA256");
    stringToSign.append('\n');
    stringToSign.append(this.timestampFull);
    stringToSign.append('\n');
    stringToSign.append(this.scopeString());
    stringToSign.append('\n');
    stringToSign.append(canonicalHash);

    final var signingKey =
      this.client.signingKey();
    final var signature =
      signingKey.sign(stringToSign.toString());

    final var requestBuilder = HttpRequest.newBuilder(uriSource);
    requestBuilder.version(HttpClient.Version.HTTP_1_1);

    final var authText =
      this.authorizationHeaderString(canonical, signature);
    requestBuilder.header("Authorization", authText);
    requestBuilder.header("User-Agent", OUserAgent.userAgent());

    for (final var entry : canonical.headers().entrySet()) {
      final var headerName = entry.getKey();
      final var headerValue = entry.getValue();
      if (!RESTRICTED_HEADERS.contains(headerName.toUpperCase())) {
        requestBuilder.header(headerName, headerValue);
      }
    }

    return requestBuilder;
  }

  protected <T> T sendGET(
    final BTQualifiedName name,
    final BTElementHandlerConstructorType<Object, T> handler)
    throws OException
  {
    this.setMethod("GET");
    this.canonicalRequest.setHeader("Host", this.hostString());

    /*
     * If we're using path-style buckets, then the resource name must be
     * set to the bucket name (such as "/bucket-0").
     */

    if (this.bucket.isPresent()) {
      switch (this.client.configuration().bucketAccessStyle()) {
        case VIRTUALHOST_STYLE -> {
          // Nothing
        }
        case PATH_STYLE -> {
          this.setResource(new OResourceRelative(List.of(this.bucket.get())));
        }
      }
    }

    final var canonical =
      this.canonicalRequest.build();
    final var canonicalHash =
      canonical.hash();

    this.setAttribute("Resource", canonical.resource().toString());
    this.setAttribute("Method", canonical.httpVerb());
    this.setAttribute("Canonical Request Hash", canonicalHash);

    final var requestBuilder =
      this.createInitialSignedRequestBuilder(canonical, canonicalHash);

    final var request =
      requestBuilder.GET()
        .build();

    final var response =
      this.executeHTTPRequest(request);

    return this.parseNonError(request, name, handler, response);
  }

  private HttpResponse<InputStream> executeHTTPRequest(
    final HttpRequest request)
    throws OException
  {
    final var http = this.httpClient();
    final HttpResponse<InputStream> response;
    try {
      response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
    } catch (final IOException e) {
      throw new OException(
        e,
        "error-io",
        Map.copyOf(this.attributes),
        Optional.empty()
      );
    } catch (final InterruptedException e) {
      throw new OException(
        e,
        "error-interruption",
        Map.copyOf(this.attributes),
        Optional.empty()
      );
    }

    this.setAttribute(
      "HTTP Status",
      Integer.toString(response.statusCode()));
    this.setAttribute(
      "Content-Type",
      response.headers().firstValue("Content-Type").orElse("")
    );

    if (response.statusCode() >= 400) {
      throw OXErrorParsing.parseError(
        this.attributes,
        this.saxParsers(),
        request.uri(),
        response.body()
      );
    }

    return response;
  }

  private URI bucketEndpoint()
    throws URISyntaxException
  {
    final var baseEndpoint =
      this.client.configuration()
        .endpoint();

    if (this.bucket.isPresent()) {
      final var bucketName =
        this.bucket.get();

      final var hostName = new StringBuilder();
      hostName.append(bucketName);
      hostName.append('.');
      hostName.append(baseEndpoint.getHost());

      return new URI(
        baseEndpoint.getScheme(),
        baseEndpoint.getUserInfo(),
        hostName.toString(),
        baseEndpoint.getPort(),
        baseEndpoint.getPath(),
        baseEndpoint.getQuery(),
        baseEndpoint.getFragment()
      );
    }

    return baseEndpoint;
  }

  private String authorizationHeaderString(
    final OCanonicalRequest canonical,
    final String signature)
  {
    final var text = new StringBuilder();
    text.append("AWS4-HMAC-SHA256 ");
    text.append("Credential=");
    text.append(this.credentialHeaderString());
    text.append(',');
    text.append("SignedHeaders=");
    text.append(
      canonical.headers()
        .keySet()
        .stream()
        .sorted()
        .map(x -> x.toLowerCase(Locale.ROOT))
        .collect(Collectors.joining(";"))
    );
    text.append(',');
    text.append("Signature=");
    text.append(signature);
    return text.toString();
  }

  private String credentialHeaderString()
  {
    final var credential = new StringBuilder();
    return switch (this.client.configuration().credentials()) {
      case final OClientAccessKeys accessKeys -> {
        credential.append(accessKeys.accessKey());
        credential.append('/');
        credential.append(this.scopeString());
        yield credential.toString();
      }
    };
  }

  private String scopeString()
  {
    final var text = new StringBuilder();
    text.append(this.timestampSigner);
    text.append('/');
    text.append(this.client.configuration().region());
    text.append('/');
    text.append("s3");
    text.append('/');
    text.append("aws4_request");
    return text.toString();
  }

  private <T> T parseNonError(
    final HttpRequest request,
    final BTQualifiedName name,
    final BTElementHandlerConstructorType<Object, T> handler,
    final HttpResponse<InputStream> response)
    throws OException
  {
    final var body =
      response.body();
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements =
      Map.of(name, handler);

    try {
      return Blackthorne.parse(
        request.uri(),
        body,
        BTPreserveLexical.PRESERVE_LEXICAL_INFORMATION,
        () -> {
          return this.saxParsers().createXMLReaderNonValidating(
            Optional.empty(),
            JXEXInclude.XINCLUDE_DISABLED
          );
        },
        rootElements
      );
    } catch (final BTException e) {
      this.attributes.putAll(e.attributes());

      final var index = 0;
      for (final var error : e.errors()) {
        final var errorKey =
          "Parse Error %d".formatted(index);
        this.setAttribute(errorKey, OXErrorParsing.showError(error));
      }
      throw new OException(
        e,
        e.errorCode(),
        Map.copyOf(this.attributes),
        e.remediatingAction()
      );
    }
  }

  protected final void setResource(
    final OResourceRelative resource)
  {
    this.canonicalRequest.setResource(resource);
  }

  protected final void queryParameterAdd(
    final String name,
    final String value)
  {
    this.canonicalRequest.addQueryParameter(name, value);
  }

  protected final void setMethod(
    final String method)
  {
    this.canonicalRequest.setMethod(method);
  }

  protected final void setBucket(
    final String inBucket)
  {
    this.bucket = Optional.of(inBucket);
  }
}
