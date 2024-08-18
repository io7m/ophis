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

import com.io7m.ophis.api.commands.OPutObjectParameters;
import com.io7m.ophis.api.commands.OPutObjectResponse;
import com.io7m.ophis.api.commands.OPutObjectType;
import com.io7m.ophis.vanilla.internal.OClient;

/**
 * A command factory.
 */

public final class OCmdPutObjectF
  implements OClientCommandFactoryType<
  OPutObjectParameters,
  OPutObjectResponse,
  OPutObjectType>
{
  /**
   * A command factory.
   */

  public OCmdPutObjectF()
  {

  }

  @Override
  public Class<OPutObjectType> commandClass()
  {
    return OPutObjectType.class;
  }

  @Override
  public OPutObjectType createCommand(
    final OClient client,
    final OPutObjectParameters parameters)
  {
    return new OCmdPutObject(client, parameters);
  }
}
