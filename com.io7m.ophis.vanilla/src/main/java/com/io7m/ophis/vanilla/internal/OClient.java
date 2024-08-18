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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.ophis.api.OClientCommandType;
import com.io7m.ophis.api.OClientConfiguration;
import com.io7m.ophis.api.OClientType;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.vanilla.internal.commands.OClientCommandCollection;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;

/**
 * The default client.
 */

public final class OClient implements OClientType
{
  private final OClientConfiguration configuration;
  private final OSigningKeyV4HMACSHA256 signingKey;
  private final OClientCommandCollection commands;
  private final HttpClient httpClient;
  private final JXEHardenedSAXParsers saxParsers;
  private final CloseableCollectionType<OException> resources;

  /**
   * The default client.
   *
   * @param inConfiguration  The configuration
   * @param inSigningKey     The signing key
   * @param inCommands       The available commands
   */

  public OClient(
    final OClientConfiguration inConfiguration,
    final OSigningKeyV4HMACSHA256 inSigningKey,
    final OClientCommandCollection inCommands)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.signingKey =
      Objects.requireNonNull(inSigningKey, "signingKey");
    this.commands =
      Objects.requireNonNull(inCommands, "commands");
    this.httpClient =
      this.configuration.httpClientProvider()
        .get();
    this.saxParsers =
      new JXEHardenedSAXParsers();

    this.resources =
      CloseableCollection.create(() -> {
        return new OException(
          "One or more resources could not be closed.",
          "error-resource",
          Map.of()
        );
      });

    this.resources.add(this.httpClient);
  }

  /**
   * @return The key used to sign requests
   */

  public OSigningKeyV4HMACSHA256 signingKey()
  {
    return this.signingKey;
  }

  /**
   * @return The SAX parsers
   */

  public JXEHardenedSAXParsers saxParsers()
  {
    return this.saxParsers;
  }

  @Override
  public OClientConfiguration configuration()
  {
    return this.configuration;
  }

  @Override
  public <P, R, C extends OClientCommandType<P, R>> OClientCommandType<P, R>
  commandFor(
    final Class<C> command,
    final P parameters)
  {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(parameters, "parameters");

    return this.commands.get(this, command, parameters);
  }

  /**
   * @return The underlying HTTP client
   */

  public HttpClient httpClient()
  {
    return this.httpClient;
  }

  @Override
  public void close()
    throws OException
  {
    this.resources.close();
  }
}
