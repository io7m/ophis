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

/**
 * The type of clients.
 */

public interface OClientType
  extends AutoCloseable
{
  /**
   * @return The configuration that was used to instantiate the client
   */

  OClientConfiguration configuration();

  /**
   * Retrieve a new command.
   *
   * @param command    The command class
   * @param parameters The parameters
   * @param <P>        The type of parameters
   * @param <R>        The type of returned values
   * @param <C>        The type of commands
   *
   * @return The new command
   *
   * @throws OException On errors
   */

  <P, R, C extends OClientCommandType<P, R>>
  OClientCommandType<P, R>
  commandFor(
    Class<C> command,
    P parameters)
    throws OException;

  /**
   * Retrieve and execute a new command.
   *
   * @param command    The command class
   * @param parameters The parameters
   * @param <P>        The type of parameters
   * @param <R>        The type of returned values
   * @param <C>        The type of commands
   *
   * @return The result of the command
   *
   * @throws OException On errors
   */

  default <P, R, C extends OClientCommandType<P, R>> R execute(
    final Class<C> command,
    final P parameters)
    throws OException
  {
    return this.commandFor(command, parameters).execute();
  }

  @Override
  void close()
    throws OException;
}
