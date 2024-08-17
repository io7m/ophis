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
import com.io7m.ophis.api.commands.OOwner;

import java.util.Map;

import static com.io7m.ophis.vanilla.internal.xml.OQName.s3Name;

/**
 * An element handler.
 */

public final class OXOwner
  implements BTElementHandlerType<OXOwner.OwnerFieldType, OOwner>
{
  private static final BTQualifiedName DISPLAY_NAME =
    s3Name("DisplayName");
  private static final BTQualifiedName ID =
    s3Name("ID");

  private String displayName = "";
  private String id = "";

  /**
   * An element handler.
   *
   * @param context The parse context
   */

  public OXOwner(
    final BTElementParsingContextType context)
  {

  }

  sealed interface OwnerFieldType
  {

  }

  record OwnerDisplayName(
    String value)
    implements OwnerFieldType
  {

  }

  record OwnerID(
    String value)
    implements OwnerFieldType
  {

  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends OwnerFieldType>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var displayHandlerStr =
      Blackthorne.forScalarString(DISPLAY_NAME);
    final var displayHandler =
      Blackthorne.mapConstructor(
        displayHandlerStr,
        OwnerDisplayName::new
      );

    final var idHandlerStr =
      Blackthorne.forScalarString(ID);
    final var idHandler =
      Blackthorne.mapConstructor(
        idHandlerStr,
        OwnerID::new
      );

    return Map.ofEntries(
      Map.entry(DISPLAY_NAME, Blackthorne.widenConstructor(displayHandler)),
      Map.entry(ID, Blackthorne.widenConstructor(idHandler))
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final OwnerFieldType result)
  {
    switch (result) {
      case final OwnerDisplayName ownerDisplayName -> {
        this.displayName = ownerDisplayName.value;
      }
      case final OwnerID ownerID -> {
        this.id = ownerID.value;
      }
    }
  }

  @Override
  public OOwner onElementFinished(
    final BTElementParsingContextType context)
  {
    return new OOwner(this.displayName, this.id);
  }
}
