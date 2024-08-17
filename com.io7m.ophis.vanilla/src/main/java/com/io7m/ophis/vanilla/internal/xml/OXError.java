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
import com.io7m.ophis.api.commands.OError;

import java.util.Map;

import static com.io7m.ophis.vanilla.internal.xml.OQName.errorName;

/**
 * An element handler.
 */

public final class OXError
  implements BTElementHandlerType<OXError.ErrorFieldType, OError>
{
  private static final BTQualifiedName ELEMENT_NAME =
    errorName("Error");
  private static final BTQualifiedName CODE =
    errorName("Code");
  private static final BTQualifiedName MESSAGE =
    errorName("Message");
  private static final BTQualifiedName RESOURCE =
    errorName("Resource");
  private static final BTQualifiedName REQUEST_ID =
    errorName("RequestId");

  private String code = "";
  private String message = "";
  private String resource = "";
  private String requestId = "";

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

  public OXError(
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
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends ErrorFieldType>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var codeHandlerS =
      Blackthorne.forScalarString(CODE);
    final var messageHandlerS =
      Blackthorne.forScalarString(MESSAGE);
    final var resourceS =
      Blackthorne.forScalarString(RESOURCE);
    final var requestIdS =
      Blackthorne.forScalarString(REQUEST_ID);

    final var codeHandler =
      Blackthorne.widenConstructor(
        Blackthorne.mapConstructor(codeHandlerS, Code::new)
      );
    final var messageHandler =
      Blackthorne.widenConstructor(
        Blackthorne.mapConstructor(messageHandlerS, Message::new)
      );
    final var resourceHandler =
      Blackthorne.widenConstructor(
        Blackthorne.mapConstructor(resourceS, Resource::new)
      );
    final var requestIdHandler =
      Blackthorne.widenConstructor(
        Blackthorne.mapConstructor(requestIdS, RequestID::new)
      );

    return Map.ofEntries(
      Map.entry(CODE, codeHandler),
      Map.entry(MESSAGE, messageHandler),
      Map.entry(RESOURCE, resourceHandler),
      Map.entry(REQUEST_ID, requestIdHandler)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final ErrorFieldType result)
  {
    switch (result) {
      case final Code r -> {
        this.code = r.value;
      }
      case final Message r -> {
        this.message = r.value;
      }
      case final RequestID r -> {
        this.requestId = r.value;
      }
      case final Resource r -> {
        this.resource = r.value;
      }
    }
  }

  @Override
  public OError onElementFinished(
    final BTElementParsingContextType context)
  {
    return new OError(
      this.code,
      this.message,
      this.resource,
      this.requestId
    );
  }

  sealed interface ErrorFieldType
  {

  }

  record Code(
    String value)
    implements ErrorFieldType
  {

  }

  record Message(
    String value)
    implements ErrorFieldType
  {

  }

  record Resource(
    String value)
    implements ErrorFieldType
  {

  }

  record RequestID(
    String value)
    implements ErrorFieldType
  {

  }
}
