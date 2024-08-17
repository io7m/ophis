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

import com.io7m.ophis.api.OClientAccessKeys;
import com.io7m.ophis.api.OClientCredentialsType;
import com.io7m.ophis.api.OException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A v4 S3 signing key.
 *
 * @param signingKey The signing key
 *
 * @see "https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html#signing-request-intro"
 */

public record OSigningKeyV4HMACSHA256(
  SecretKeySpec signingKey)
  implements OSigningKeyType
{
  /**
   * A v4 S3 signing key.
   *
   * @param signingKey The signing key
   */

  public OSigningKeyV4HMACSHA256
  {
    Objects.requireNonNull(signingKey, "signingKey");
  }

  /**
   * Create a new signing key.
   *
   * @param access  The access data
   * @param date    The date
   * @param region  The region
   * @param service The service name
   *
   * @return A key
   *
   * @throws OException On errors
   */

  public static OSigningKeyV4HMACSHA256 create(
    final OClientCredentialsType access,
    final OffsetDateTime date,
    final String region,
    final String service)
    throws OException
  {
    Objects.requireNonNull(access, "access");
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(region, "region");
    Objects.requireNonNull(service, "service");

    return switch (access) {
      case final OClientAccessKeys accessKeys -> {
        yield createForAccess(accessKeys, date, region, service);
      }
    };
  }

  private static OSigningKeyV4HMACSHA256 createForAccess(
    final OClientAccessKeys access,
    final OffsetDateTime date,
    final String region,
    final String service)
    throws OException
  {
    final var attributes = Map.ofEntries(
      Map.entry("Algorithm", "HmacSHA256")
    );

    try {
      final var aws4SecretKey =
        "AWS4" + access.secret();

      final var dateKey =
        composeHMAC(
          aws4SecretKey.getBytes(UTF_8),
          OTimeFormatters.signerFormatString().getBytes(UTF_8)
        );

      final var dateRegionKey =
        composeHMAC(
          dateKey,
          region.getBytes(UTF_8)
        );

      final var dateRegionServiceKey =
        composeHMAC(
          dateRegionKey,
          service.getBytes(UTF_8)
        );

      final var signingKey =
        composeHMAC(
          dateRegionServiceKey,
          "aws4_request".getBytes(UTF_8)
        );

      return new OSigningKeyV4HMACSHA256(new SecretKeySpec(signingKey, "HmacSHA256"));
    } catch (final NoSuchAlgorithmException e) {
      throw new OException(
        e,
        "error-no-such-algorithm",
        attributes,
        Optional.empty());
    } catch (final InvalidKeyException e) {
      throw new OException(
        e,
        "error-invalid-key",
        attributes,
        Optional.empty());
    }
  }

  private static byte[] composeHMAC(
    final byte[] key,
    final byte[] data)
    throws NoSuchAlgorithmException, InvalidKeyException
  {
    final var mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    mac.update(data);
    return mac.doFinal();
  }

  @Override
  public String sign(
    final String text)
    throws OException
  {
    Objects.requireNonNull(text, "text");

    final var attributes = Map.ofEntries(
      Map.entry("Algorithm", "HmacSHA256")
    );

    try {
      final var inputBytes =
        text.getBytes(UTF_8);
      final var algorithm =
        Mac.getInstance("HmacSHA256");

      algorithm.init(this.signingKey);
      algorithm.update(inputBytes);

      return HexFormat.of().formatHex(algorithm.doFinal());
    } catch (final NoSuchAlgorithmException e) {
      throw new OException(
        e,
        "error-no-such-algorithm",
        attributes,
        Optional.empty());
    } catch (final InvalidKeyException e) {
      throw new OException(
        e,
        "error-invalid-key",
        attributes,
        Optional.empty());
    }
  }
}
