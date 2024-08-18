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


package com.io7m.ophis.tests;

import com.io7m.ophis.vanilla.internal.OCanonicalRequest;
import com.io7m.ophis.vanilla.internal.OResourceRelative;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OCanonicalRequestTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OCanonicalRequestTest.class);

  private static void dumpRequest(
    final OCanonicalRequest request)
  {
    LOG.debug("-- Start 8<--");
    LOG.debug("{}", request);
    LOG.debug("-->8 End ----");
  }

  @Test
  public void testRequestEmpty()
  {
    final var request =
      OCanonicalRequest.builder()
        .build();

    dumpRequest(request);

    assertEquals("GET", request.httpVerb());
    assertEquals("", request.resource().toString());
    assertEquals(Map.of(), request.headers());
    assertEquals(List.of(), request.queryParameters());
    assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", request.hashedPayload());
  }

  @Test
  public void testRequestPut()
  {
    final var request =
      OCanonicalRequest.builder()
        .setResource(OResourceRelative.parse("example.txt"))
        .setMethod("PUT")
        .setHeader("Content-Length", "7")
        .setHeader("Content-MD5", "4TTO0xKzUR2IlD1XzNcMgw==")
        .setHeader("Expires", "Mon, 19 Aug 2024 09:18:47 GMT")
        .setHeader("Content-Type", "text/plain")
        .setHeader("Host", "general.localhost:30000")
        .setHeader("x-amz-date", "20240818T091847Z")
        .setHeader("x-amz-checksum-sha256", "b22b009134622b6508d756f1062455d71a7026594eacb0badf81f4f677929ebe")
        .setHashedPayload("b22b009134622b6508d756f1062455d71a7026594eacb0badf81f4f677929ebe")
        .build();

    dumpRequest(request);

    assertEquals("GET", request.httpVerb());
    assertEquals("example.txt", request.resource().toString());
    assertEquals(Map.of(), request.headers());
    assertEquals(List.of(), request.queryParameters());
    assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", request.hashedPayload());
  }
}
