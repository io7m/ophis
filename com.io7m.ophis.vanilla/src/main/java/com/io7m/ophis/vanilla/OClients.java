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


package com.io7m.ophis.vanilla;

import com.io7m.ophis.api.OClientConfiguration;
import com.io7m.ophis.api.OClientFactoryType;
import com.io7m.ophis.api.OClientType;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.vanilla.internal.OClient;
import com.io7m.ophis.vanilla.internal.OSigningKeyV4HMACSHA256;
import com.io7m.ophis.vanilla.internal.commands.OClientCommandCollection;

import java.time.OffsetDateTime;

/**
 * The default client factory.
 */

public final class OClients implements OClientFactoryType
{
  private final OClientCommandCollection commands;

  /**
   * The default client factory.
   */

  public OClients()
  {
    this.commands =
      OClientCommandCollection.createFromServiceLoader();
  }

  @Override
  public OClientType createClient(
    final OClientConfiguration configuration)
    throws OException
  {
    final var signingKey =
      OSigningKeyV4HMACSHA256.create(
        configuration.credentials(),
        OffsetDateTime.now(),
        configuration.region(),
        "s3"
      );

    return new OClient(configuration, signingKey, this.commands);
  }
}
