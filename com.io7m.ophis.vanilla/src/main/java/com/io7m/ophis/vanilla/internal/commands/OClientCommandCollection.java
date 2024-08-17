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

import com.io7m.ophis.api.OClientCommandType;
import com.io7m.ophis.vanilla.internal.OClient;

import java.util.HashMap;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * A command factory collection.
 */

public final class OClientCommandCollection
{
  private final HashMap<Class<?>, OClientCommandFactoryType<?, ?, ?>> commands;

  /**
   * Create an empty collection.
   */

  public OClientCommandCollection()
  {
    this.commands = new HashMap<>();
  }

  /**
   * Register a command factory.
   *
   * @param factory The command factory
   */

  public void register(
    final OClientCommandFactoryType<?, ?, ?> factory)
  {
    Objects.requireNonNull(factory, "factory");
    final Class<?> clazz = factory.commandClass();
    if (this.commands.containsKey(clazz)) {
      throw new IllegalStateException(
        "A command factory already exists for %s".formatted(clazz)
      );
    }
    this.commands.put(clazz, factory);
  }

  /**
   * Create a new collection, reading all available services from {@link ServiceLoader}.
   *
   * @return The collection
   */

  public static OClientCommandCollection createFromServiceLoader()
  {
    final var collection = new OClientCommandCollection();

    final var loader =
      ServiceLoader.load(OClientCommandFactoryType.class);

    for (final var factory : loader) {
      collection.register(factory);
    }
    return collection;
  }

  /**
   * Get a command.
   *
   * @param client     The client
   * @param clazz      The command class
   * @param parameters The parameters
   * @param <P>        The type of parameters
   * @param <R>        The type of results
   * @param <C>        The command type
   *
   * @return The command
   */

  @SuppressWarnings("unchecked")
  public <P, R, C extends OClientCommandType<P, R>> C get(
    final OClient client,
    final Class<C> clazz,
    final P parameters)
  {
    Objects.requireNonNull(client, "client");
    Objects.requireNonNull(clazz, "clazz");
    Objects.requireNonNull(parameters, "parameters");

    final var factory = this.commands.get(clazz);
    if (factory == null) {
      throw new IllegalStateException(
        "No command implementation available for %s".formatted(clazz)
      );
    }

    return (C) factory.createCommand(client, cast(parameters));
  }

  @SuppressWarnings("unchecked")
  private static <A, B> B cast(
    final A x)
  {
    return (B) (Object) x;
  }
}
