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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record OResourceRelative(
  List<String> segments)
{
  public OResourceRelative
  {
    segments = List.copyOf(segments);
  }

  public static OResourceRelative parse(
    final String key)
  {
    return new OResourceRelative(
      Stream.of(key.replaceAll("/+", "/").split("/"))
        .filter(s -> !s.isEmpty())
        .filter(s -> !s.isBlank())
        .toList()
    );
  }

  @Override
  public String toString()
  {
    return this.segments.stream()
      .map(OURLEncode::urlEncode)
      .collect(Collectors.joining("/"));
  }

  public static OResourceRelative empty()
  {
    return new OResourceRelative(List.of());
  }
}
