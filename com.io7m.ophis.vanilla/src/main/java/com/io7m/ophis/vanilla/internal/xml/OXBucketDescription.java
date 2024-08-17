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


package com.io7m.ophis.vanilla.internal.xml;

import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.blackthorne.core.Blackthorne;
import com.io7m.ophis.api.commands.OBucketDescription;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.io7m.ophis.vanilla.internal.xml.OQName.s3Name;

/**
 * An element handler.
 */

public final class OXBucketDescription
  implements BTElementHandlerType<Object, OBucketDescription>
{
  private static final BTQualifiedName CREATION_DATE =
    s3Name("CreationDate");
  private static final BTQualifiedName NAME =
    s3Name("Name");

  private String name;
  private OffsetDateTime creationDate;

  /**
   * An element handler.
   *
   * @param context The parse context
   */

  public OXBucketDescription(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        CREATION_DATE,
        Blackthorne.forScalarFromString(CREATION_DATE, OffsetDateTime::parse)
      ),
      Map.entry(NAME, Blackthorne.forScalarString(NAME))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final String r -> {
        this.name = r;
      }
      case final OffsetDateTime r -> {
        this.creationDate = r;
      }
      default -> {

      }
    }
  }

  @Override
  public OBucketDescription onElementFinished(
    final BTElementParsingContextType context)
  {
    return new OBucketDescription(this.name, this.creationDate);
  }
}
