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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The various time formatters required to talk to S3.
 */

public final class OTimeFormatters
{
  private static final ZoneId UTC =
    ZoneId.of("Z");

  private static final DateTimeFormatter AMZ_DATE_FORMAT =
    DateTimeFormatter.ofPattern(
      "yyyyMMdd'T'HHmmss'Z'",
      Locale.US
    ).withZone(UTC);

  private static final DateTimeFormatter HTTP_HEADER_DATE_FORMAT =
    DateTimeFormatter.ofPattern(
        "EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'",
        Locale.US)
      .withZone(UTC);

  private static final DateTimeFormatter SIGNER_DATE_FORMAT =
    DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US)
      .withZone(UTC);

  private OTimeFormatters()
  {

  }

  /**
   * @return The current time as an AMZ date format string
   */

  public static String amzDateFormatString()
  {
    return AMZ_DATE_FORMAT.format(OffsetDateTime.now(UTC));
  }

  /**
   * @return The current time as a signer date format string
   */

  public static String signerFormatString()
  {
    return SIGNER_DATE_FORMAT.format(OffsetDateTime.now(UTC));
  }

  /**
   * @return The HTTP header date format
   *
   * @see "https://www.rfc-editor.org/rfc/rfc7234#section-5.3"
   */

  public static DateTimeFormatter httpHeaderFormat()
  {
    return HTTP_HEADER_DATE_FORMAT;
  }
}
