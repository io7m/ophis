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

import java.time.OffsetDateTime;
import java.util.Optional;

@ImmutablesStyleType
@Value.Immutable
public interface OObjectContentsType
{
  Optional<String> checksumAlgorithm();

  Optional<String> eTag();

  Optional<String> key();

  @Value.Default
  default OffsetDateTime lastModified()
  {
    return OffsetDateTime.now();
  }

  Optional<OOwner> owner();

  @Value.Default
  default long size()
  {
    return 0L;
  }

  @Value.Default
  default ORestoreStatus restoreStatus()
  {
    return new ORestoreStatus(false, Optional.empty());
  }

  @Value.Default
  default String storageClass()
  {
    return "";
  }
}
