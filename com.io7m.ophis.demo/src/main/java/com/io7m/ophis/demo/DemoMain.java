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


package com.io7m.ophis.demo;

import com.io7m.ophis.api.OClientAccessKeys;
import com.io7m.ophis.api.OClientConfiguration;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.api.commands.OListObjectsParameters;
import com.io7m.ophis.api.commands.OListObjectsType;
import com.io7m.ophis.api.commands.OObjectDatas;
import com.io7m.ophis.api.commands.OPutObjectParameters;
import com.io7m.ophis.api.commands.OPutObjectType;
import com.io7m.ophis.vanilla.OClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * S3 client (Demo).
 */

public final class DemoMain
{
  private static final Logger LOG =
    LoggerFactory.getLogger(DemoMain.class);

  private DemoMain()
  {

  }

  /**
   * S3 client (Demo).
   *
   * @param args The arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    final var endpoint =
      System.getProperty("ophis.endpoint");
    final var access =
      System.getProperty("ophis.accessKey");
    final var secret =
      System.getProperty("ophis.secret");

    Objects.requireNonNull(endpoint, "endpoint");
    Objects.requireNonNull(access, "access");
    Objects.requireNonNull(secret, "secret");

    final var clients =
      new OClients();

    final var configuration =
      OClientConfiguration.builder()
        .setEndpoint(URI.create(endpoint))
        .setCredentials(new OClientAccessKeys(access, secret))
        .build();

    final var tempFile =
      Paths.get("/tmp/file.txt");

    try (var client = clients.createClient(configuration)) {
      Files.writeString(tempFile, "Hello!\n");

      {
        final var r =
          client.execute(
            OPutObjectType.class,
            OPutObjectParameters.builder()
              .setExpires(OffsetDateTime.now().plusDays(1L))
              .setBucketName("general")
              .setContentType("text/plain")
              .setKey("example.txt")
              .setData(OObjectDatas.ofFile(tempFile))
              .build()
          );

        LOG.debug("Result: {}", r);
      }

      {
        final var r = client.execute(
          OListObjectsType.class,
          OListObjectsParameters.builder()
            .setBucketName("general")
            .build()
        );

        LOG.debug("Result: {}", r);
      }
    } catch (final OException e) {
      LOG.error("{}: {}", e.errorCode(), e.getMessage());
      for (final var entry : e.attributes().entrySet()) {
        LOG.error("  {}: {}", entry.getKey(), entry.getValue());
      }
      LOG.error("Stacktrace: ", e);
    }
  }
}
