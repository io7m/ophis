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
import com.io7m.ophis.vanilla.internal.OCanonicalRequest;
import com.io7m.ophis.vanilla.internal.OClient;
import com.io7m.ophis.vanilla.internal.OTimeFormatters;
import com.io7m.ophis.vanilla.internal.OUserAgent;
import com.io7m.ophis.vanilla.internal.xml.OXErrorParsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class OCmdAbstract<P, R>
{
  private final P parameters;
  private final OClient client;
  private final HashMap<String, String> attributes;
  private final OCanonicalRequest.Builder canonicalRequest;
  private final String timestampFull;
  private final String timestampSigner;

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

    this.canonicalRequest.setHeader("Host", this.hostString());
    this.canonicalRequest.setHeader("x-amz-date", this.timestampFull);
  }

  private String hostString()
  {
    final var endpoint =
      this.client.configuration().endpoint();
    final var host =
      endpoint.getHost();
    final var port =
      endpoint.getPort();

    if (port >= 0) {
      return host + ":" + port;
    }
    return host;
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

  protected <T> T sendRequest(
    final BTQualifiedName name,
    final BTElementHandlerConstructorType<Object, T> handler)
    throws OException
  {
    final var canonical =
      this.canonicalRequest.build();
    final var canonicalHash =
      canonical.hash();

    this.setAttribute("Resource", canonical.resource());
    this.setAttribute("Method", canonical.httpVerb());
    this.setAttribute("Canonical Request Hash", canonicalHash);

    final var uriSource =
      canonical.queryURI(this.client.configuration().endpoint());

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
      if (!headerName.equalsIgnoreCase("Host")) {
        requestBuilder.header(headerName, headerValue);
      }
    }

    final var request =
      requestBuilder
        .GET()
        .build();

    final var http =
      this.httpClient();

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

    return this.parseNonError(request, name, handler, response);
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements =
      Map.of(name, handler);

    try {
      return Blackthorne.parse(
        request.uri(),
        response.body(),
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

      final int index = 0;
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
    final String resource)
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
}
