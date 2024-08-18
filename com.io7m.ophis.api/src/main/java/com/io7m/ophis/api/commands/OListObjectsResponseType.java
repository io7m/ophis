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


package com.io7m.ophis.api.commands;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@ImmutablesStyleType
@Value.Immutable
public interface OListObjectsResponseType
{
  @Value.Default
  default boolean isTruncated()
  {
    return false;
  }

  List<OObjectContents> contents();

  String name();

  String prefix();

  @Value.Default
  default String delimiter()
  {
    return "/";
  }

  @Value.Default
  default int maxKeys()
  {
    return 1000;
  }

  @Value.Default
  default String encoding()
  {
    return "UTF-8";
  }

  @Value.Default
  default int keyCount()
  {
    return 0;
  }

  Optional<String> continuationToken();

  Optional<String> nextContinuationToken();

  @Value.Default
  default String startAfter()
  {
    return "";
  }
}
