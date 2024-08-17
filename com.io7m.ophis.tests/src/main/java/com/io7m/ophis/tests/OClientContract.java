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

import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.ophis.api.OClientAccessKeys;
import com.io7m.ophis.api.OClientConfiguration;
import com.io7m.ophis.api.OClientCredentialsType;
import com.io7m.ophis.api.OClientType;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.vanilla.OClients;
import com.io7m.zelador.test_extension.CloseableResourcesType;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;

@Tag("integration")
@Tag("client")
@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.ophis", disabledIfUnsupported = true)
public abstract class OClientContract
{
  private static final OClientCredentialsType CREDENTIALS_GOOD =
    new OClientAccessKeys(
      "buvHtDywtJbpc5ZoptQl",
      "NdvOkckxvPeawuikDrlO1fbt1V2an6f3RaxAYLO0"
    );

  private static OClients CLIENTS;
  private static OMinIOFixture MINIO;
  private static Path DIRECTORY;

  static String minioPassword()
  {
    return "12345678";
  }

  static String minioUser()
  {
    return "someone";
  }

  static OClientCredentialsType credentials()
  {
    return CREDENTIALS_GOOD;
  }

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType supervisor,
    final @TempDir Path directory)
    throws Exception
  {
    CLIENTS =
      new OClients();
    DIRECTORY =
      directory;
    MINIO =
      OFixtures.minio(supervisor);
  }

  protected final OMinIOFixture minio()
  {
    return MINIO;
  }

  protected final OClientType client()
    throws OException
  {
    return CLIENTS.createClient(
      OClientConfiguration.builder()
        .setHttpClientProvider(HttpClient::newHttpClient)
        .setEndpoint(URI.create("http://localhost:" + MINIO.port()))
        .setCredentials(CREDENTIALS_GOOD)
        .build()
    );
  }

  @BeforeEach
  public void setupEach(
    final @ErvillaCloseAfterSuite EContainerSupervisorType supervisor,
    final CloseableResourcesType closeables)
    throws Exception
  {
    MINIO.reset();
    MINIO.createUser(
      minioUser(),
      minioPassword(),
      CREDENTIALS_GOOD
    );
    MINIO.createBucket(
      "example-bucket-0"
    );
  }

}
