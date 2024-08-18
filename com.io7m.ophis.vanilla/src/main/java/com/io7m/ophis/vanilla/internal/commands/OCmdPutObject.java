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

import com.io7m.ophis.api.OException;
import com.io7m.ophis.api.commands.OObjectData;
import com.io7m.ophis.api.commands.OPutObjectParameters;
import com.io7m.ophis.api.commands.OPutObjectResponse;
import com.io7m.ophis.api.commands.OPutObjectType;
import com.io7m.ophis.vanilla.internal.OClient;
import com.io7m.ophis.vanilla.internal.OResourceRelative;
import com.io7m.ophis.vanilla.internal.OTimeFormatters;

import java.net.http.HttpHeaders;

/**
 * PutObject.
 */

public final class OCmdPutObject
  extends OCmdAbstract<OPutObjectParameters, OPutObjectResponse>
  implements OPutObjectType
{
  OCmdPutObject(
    final OClient client,
    final OPutObjectParameters parameters)
  {
    super(client, parameters);
  }

  @Override
  public OPutObjectResponse execute()
    throws OException
  {
    final var parameters =
      this.parameters();
    final var data =
      parameters.data();

    this.setBucket(parameters.bucketName());
    this.setHeader("Content-Type", parameters.contentType());
    this.setHeader("Content-MD5", data.md5());
    this.setHeader("Content-Length", Long.toUnsignedString(data.size()));
    this.setHeader("x-amz-checksum-sha256", data.sha256());

    parameters.expires().ifPresent(time -> {
      this.setHeader(
        "Expires",
        OTimeFormatters.httpHeaderFormat()
          .format(time)
      );
    });

    return this.sendPUT(
      data,
      OResourceRelative.parse(parameters.key()),
      (final HttpHeaders headers) -> {
        return OPutObjectResponse.builder()
          .build();
      }
    );
  }
}
