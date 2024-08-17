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


package com.io7m.ophis.vanilla.internal;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The immutable canonical form of a request, used for signatures.
 *
 * @param httpVerb        The HTTP verb (such as "GET")
 * @param resource        The resource (such as "/")
 * @param queryParameters The query parameters
 * @param headers         The headers
 * @param hashedPayload   The SHA-256 hash of the payload
 *
 * @see "https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html"
 */

public record OCanonicalRequest(
  String httpVerb,
  String resource,
  List<Map.Entry<String, String>> queryParameters,
  Map<String, String> headers,
  String hashedPayload)
{
  /**
   * The immutable canonical form of a request, used for signatures.
   *
   * @param httpVerb        The HTTP verb (such as "GET")
   * @param resource        The resource (such as "/")
   * @param queryParameters The query parameters
   * @param headers         The headers
   * @param hashedPayload   The SHA-256 hash of the payload
   *
   * @see "https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html"
   */

  public OCanonicalRequest
  {
    Objects.requireNonNull(httpVerb, "httpVerb");
    Objects.requireNonNull(resource, "resource");
    queryParameters = List.copyOf(queryParameters);
    headers = Map.copyOf(headers);
    Objects.requireNonNull(hashedPayload, "hashedPayload");
  }

  private static String encodeHeaderEntry(
    final Map.Entry<String, String> e)
  {
    return e.getKey().toLowerCase(Locale.ROOT) + ":" + e.getValue().trim() + "\n";
  }

  private static String encodeQueryEntry(
    final Map.Entry<String, String> e)
  {
    return OURLEncode.urlEncode(e.getKey())
           + "="
           + OURLEncode.urlEncode(e.getValue());
  }

  /**
   * @return A new canonical request builder
   */

  public static Builder builder()
  {
    return new Builder();
  }

  @Override
  public String toString()
  {
    final var text = new StringBuilder(256);
    text.append(this.httpVerb);
    text.append('\n');

    text.append(this.encodeResource());
    text.append('\n');

    text.append(
      this.queryParameters
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(OCanonicalRequest::encodeQueryEntry)
        .collect(Collectors.joining("&"))
    );
    text.append('\n');

    text.append(
      this.headers.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(OCanonicalRequest::encodeHeaderEntry)
        .collect(Collectors.joining(""))
    );
    text.append('\n');

    text.append(
      this.headers.keySet()
        .stream()
        .sorted()
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(Collectors.joining(";"))
    );
    text.append('\n');

    text.append(this.hashedPayload);
    return text.toString();
  }

  private String encodeResource()
  {
    final var result =
      new StringBuilder("/");

    final var segments =
      this.resource.split("/");

    for (final var segment : segments) {
      if (!segment.isEmpty()) {
        result.append("/");
        result.append(OURLEncode.urlEncode(segment));
      }
    }

    return result.toString().replaceAll("/+", "/");
  }

  /**
   * Construct a query URI based on this canonical request and the given
   * endpoint.
   *
   * @param endpoint The base endpoint
   *
   * @return The resulting URI
   */

  public URI queryURI(
    final URI endpoint)
  {
    final var text = new StringBuilder(256);
    text.append(endpoint);
    if (!endpoint.toString().endsWith("/")) {
      text.append('/');
    }
    text.append('?');
    text.append(
      this.queryParameters
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(OCanonicalRequest::encodeQueryEntry)
        .collect(Collectors.joining("&"))
    );
    return URI.create(text.toString());
  }

  /**
   * Calculate the hex-formatted SHA-256 hash of the canonical request.
   *
   * @return The hash
   */

  public String hash()
  {
    try {
      final var digest =
        MessageDigest.getInstance("SHA-256");

      final var textBytes =
        this.toString()
          .getBytes(StandardCharsets.UTF_8);

      return HexFormat.of()
        .formatHex(digest.digest(textBytes));
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * A mutable canonical request builder.
   */

  public static final class Builder
  {
    private String method = "GET";
    private String resource = "/";

    /**
     * The SHA-256 of the empty string.
     */

    private String hashedPayload =
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private ArrayList<Map.Entry<String, String>> queryParameters;
    private SortedMap<String, String> headers;

    private Builder()
    {
      this.queryParameters =
        new ArrayList<>();
      this.headers =
        new TreeMap<>();
    }

    /**
     * Set the HTTP method.
     *
     * @param inMethod The method
     *
     * @return this
     */

    public Builder setMethod(
      final String inMethod)
    {
      this.method =
        Objects.requireNonNull(inMethod, "method");

      return this;
    }

    /**
     * Set the HTTP resource.
     *
     * @param inResource The resource
     *
     * @return this
     */

    public Builder setResource(
      final String inResource)
    {
      this.resource =
        Objects.requireNonNull(inResource, "resource");

      return this;
    }

    /**
     * Set the hash of the payload.
     *
     * @param inPayload The hash
     *
     * @return this
     */

    public Builder setHashedPayload(
      final String inPayload)
    {
      this.hashedPayload =
        Objects.requireNonNull(inPayload, "payload");

      return this;
    }

    /**
     * Add a query parameter.
     *
     * @param name  The parameter name
     * @param value The parameter value
     *
     * @return this
     */

    public Builder addQueryParameter(
      final String name,
      final String value)
    {
      this.queryParameters.add(
        Map.entry(
          Objects.requireNonNull(name, "name"),
          Objects.requireNonNull(value, "value")
        )
      );

      return this;
    }

    /**
     * Add a header.
     *
     * @param name  The header name
     * @param value The header value
     *
     * @return this
     */

    public Builder setHeader(
      final String name,
      final String value)
    {
      this.headers.put(
        Objects.requireNonNull(name, "name"),
        Objects.requireNonNull(value, "value")
      );
      return this;
    }

    /**
     * Build the canonical request.
     *
     * @return The immutable request
     */

    public OCanonicalRequest build()
    {
      return new OCanonicalRequest(
        this.method,
        this.resource,
        this.queryParameters,
        this.headers,
        this.hashedPayload
      );
    }
  }
}
