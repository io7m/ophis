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


package com.io7m.ophis.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The type of client configurations.
 */

@Value.Immutable
@ImmutablesStyleType
public interface OClientConfigurationType
{
  /**
   * @return The endpoint against which to make requests
   */

  URI endpoint();

  /**
   * @return The bucket access style
   */

  @Value.Default
  default OClientBucketAccessStyle bucketAccessStyle()
  {
    return OClientBucketAccessStyle.VIRTUALHOST_STYLE;
  }

  /**
   * The S3 region. For local installations such as in {@code minio}, use "us-east-1".
   *
   * @return The S3 region
   *
   * @see "https://github.com/minio/minio/discussions/15063"
   * @see "https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html#signing-request-intro"
   */

  @Value.Default
  default String region()
  {
    return "us-east-1";
  }

  /**
   * @return The client credentials
   */

  OClientCredentialsType credentials();

  /**
   * @return An HTTP client provider
   */

  @Value.Default
  default Supplier<HttpClient> httpClientProvider()
  {
    return HttpClient::newHttpClient;
  }
}
