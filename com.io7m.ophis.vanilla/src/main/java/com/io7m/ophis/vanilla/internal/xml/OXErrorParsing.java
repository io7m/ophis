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
import com.io7m.blackthorne.core.BTException;
import com.io7m.blackthorne.core.BTParseError;
import com.io7m.blackthorne.core.BTPreserveLexical;
import com.io7m.blackthorne.core.BTQualifiedName;
import com.io7m.blackthorne.core.Blackthorne;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXEXInclude;
import com.io7m.ophis.api.OException;
import com.io7m.ophis.api.commands.OError;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Functions to parse error responses.
 */

public final class OXErrorParsing
{
  private OXErrorParsing()
  {

  }

  /**
   * Format the given parse error as a simple string.
   *
   * @param error The parse error
   *
   * @return The serialized error
   */

  public static String showError(
    final BTParseError error)
  {
    Objects.requireNonNull(error, "error");
    return showLexical(error.lexical()) + ": " + error.message();
  }

  private static String showLexical(
    final LexicalPosition<URI> lexical)
  {
    return String.format("%d:%d", lexical.line(), lexical.column());
  }

  /**
   * Convert the given error response into a structured error exception.
   *
   * @param errorAttributes The error attributes
   * @param saxParsers      The SAX parsers
   * @param source          The source
   * @param response        The response
   *
   * @return The error
   */

  public static OException parseError(
    final HashMap<String, String> errorAttributes,
    final JXEHardenedSAXParsers saxParsers,
    final URI source,
    final InputStream response)
  {
    Objects.requireNonNull(errorAttributes, "errorAttributes");
    Objects.requireNonNull(saxParsers, "saxParsers");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(response, "response");

    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, OError>> rootElements =
      Map.of(OXError.elementName(), OXError::new);

    try {
      final var error =
        Blackthorne.parse(
          source,
          response,
          BTPreserveLexical.PRESERVE_LEXICAL_INFORMATION,
          () -> {
            return saxParsers.createXMLReaderNonValidating(
              Optional.empty(),
              JXEXInclude.XINCLUDE_DISABLED
            );
          },
          rootElements
        );

      return new OException(
        error.message(),
        error.code(),
        Map.copyOf(errorAttributes),
        Optional.empty()
      );
    } catch (final BTException e) {
      errorAttributes.putAll(e.attributes());

      final int index = 0;
      for (final var error : e.errors()) {
        final var errorKey =
          "Parse Error %d".formatted(index);
        errorAttributes.put(errorKey, showError(error));
      }
      return new OException(
        "Failed to parse an Error value: " + e.message(),
        e,
        e.errorCode(),
        Map.copyOf(errorAttributes),
        e.remediatingAction()
      );
    }
  }
}
