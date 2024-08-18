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
import com.io7m.blackthorne.core.BTIgnoreUnrecognizedElements;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.blackthorne.core.Blackthorne;
import com.io7m.ophis.api.commands.ORestoreStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static com.io7m.ophis.vanilla.internal.xml.OQName.errorName;

/**
 * An element handler.
 */

public final class OXRestoreStatus
  implements BTElementHandlerType<Object, ORestoreStatus>
{
  private static final BTQualifiedName ELEMENT_NAME =
    errorName("RestoreStatus");
  private static final BTQualifiedName IS_RESTORE_IN_PROGRESS =
    errorName("IsRestoreInProgress");
  private static final BTQualifiedName RESTORE_EXPIRY_DATE =
    errorName("RestoreExpiryDate");

  private Boolean inProgress = false;
  private Optional<OffsetDateTime> expiryDate = Optional.empty();

  @Override
  public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return BTIgnoreUnrecognizedElements.IGNORE_UNRECOGNIZED_ELEMENTS;
  }

  /**
   * An element handler.
   *
   * @param context The parse context
   */

  public OXRestoreStatus(
    final BTElementParsingContextType context)
  {

  }

  /**
   * @return The element name
   */

  public static BTQualifiedName elementName()
  {
    return ELEMENT_NAME;
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        IS_RESTORE_IN_PROGRESS,
        Blackthorne.mapConstructor(
          Blackthorne.forScalarString(IS_RESTORE_IN_PROGRESS),
          Boolean::parseBoolean
        )
      ),
      Map.entry(
        RESTORE_EXPIRY_DATE,
        Blackthorne.mapConstructor(
          Blackthorne.forScalarString(RESTORE_EXPIRY_DATE),
          OffsetDateTime::parse
        )
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final Boolean ff -> {
        this.inProgress = ff;
      }
      case final OffsetDateTime ff -> {
        this.expiryDate = Optional.of(ff);
      }
      default -> {
        throw new IllegalStateException("Unexpected value: " + result);
      }
    }
  }

  @Override
  public ORestoreStatus onElementFinished(
    final BTElementParsingContextType context)
  {
    return new ORestoreStatus(
      this.inProgress,
      this.expiryDate
    );
  }
}
