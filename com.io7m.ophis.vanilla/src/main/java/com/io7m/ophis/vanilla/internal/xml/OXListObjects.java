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
import com.io7m.ophis.api.commands.OListObjectsResponse;
import com.io7m.ophis.api.commands.OObjectContents;
import org.xml.sax.Attributes;

import java.util.Map;

import static com.io7m.ophis.vanilla.internal.xml.OQName.s3Name;

/**
 * An element handler.
 */

public final class OXListObjects
  implements BTElementHandlerType<Object, OListObjectsResponse>
{
  private static final BTQualifiedName ELEMENT_NAME =
    s3Name("ListBucketResult");
  private static final BTQualifiedName IS_TRUNCATED =
    s3Name("IsTruncated");
  private static final BTQualifiedName NAME =
    s3Name("Name");
  private static final BTQualifiedName PREFIX =
    s3Name("Prefix");
  private static final BTQualifiedName DELIMITER =
    s3Name("Delimiter");
  private static final BTQualifiedName MAX_KEYS =
    s3Name("MaxKeys");
  private static final BTQualifiedName ENCODING_TYPE =
    s3Name("EncodingType");
  private static final BTQualifiedName KEY_COUNT =
    s3Name("KeyCount");
  private static final BTQualifiedName CONTINUATION_TOKEN =
    s3Name("ContinuationToken");
  private static final BTQualifiedName NEXT_CONTINUATION_TOKEN =
    s3Name("NextContinuationToken");
  private static final BTQualifiedName START_AFTER =
    s3Name("StartAfter");

  private OListObjectsResponse.Builder builder;

  /**
   * @return The root element name
   */

  public static BTQualifiedName elementName()
  {
    return ELEMENT_NAME;
  }

  /**
   * An element handler.
   *
   * @param context The parse context
   */

  public OXListObjects(
    final BTElementParsingContextType context)
  {

  }

  sealed interface FieldType
  {
    record FieldIsTruncated(boolean value) implements FieldType
    {
    }

    record FieldName(String value) implements FieldType
    {
    }

    record FieldPrefix(String value) implements FieldType
    {
    }

    record FieldDelimiter(String value) implements FieldType
    {
    }

    record FieldMaxKeys(int value) implements FieldType
    {
    }

    record FieldEncoding(String value) implements FieldType
    {
    }

    record FieldKeyCount(int value) implements FieldType
    {
    }

    record FieldContinuationToken(String value) implements FieldType
    {
    }

    record FieldNextContinuationToken(String value) implements FieldType
    {
    }

    record FieldStartAfter(String value) implements FieldType
    {
    }
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var isTruncatedHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(IS_TRUNCATED),
        s -> new FieldType.FieldIsTruncated(Boolean.parseBoolean(s))
      );
    final var nameHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(NAME),
        FieldType.FieldName::new
      );
    final var prefixHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(PREFIX),
        FieldType.FieldPrefix::new
      );
    final var delimiterHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(DELIMITER),
        FieldType.FieldDelimiter::new
      );
    final var maxKeysHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(MAX_KEYS),
        s -> new FieldType.FieldMaxKeys(Integer.parseUnsignedInt(s))
      );
    final var keyCountHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(KEY_COUNT),
        s -> new FieldType.FieldKeyCount(Integer.parseUnsignedInt(s))
      );
    final var continuationHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(CONTINUATION_TOKEN),
        FieldType.FieldContinuationToken::new
      );
    final var nextContinuationHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(NEXT_CONTINUATION_TOKEN),
        FieldType.FieldNextContinuationToken::new
      );
    final var startAfterHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(START_AFTER),
        FieldType.FieldStartAfter::new
      );
    final var encodingHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(ENCODING_TYPE),
        FieldType.FieldEncoding::new
      );

    return Map.ofEntries(
      Map.entry(IS_TRUNCATED, isTruncatedHandler),
      Map.entry(NAME, nameHandler),
      Map.entry(PREFIX, prefixHandler),
      Map.entry(DELIMITER, delimiterHandler),
      Map.entry(MAX_KEYS, maxKeysHandler),
      Map.entry(KEY_COUNT, keyCountHandler),
      Map.entry(CONTINUATION_TOKEN, continuationHandler),
      Map.entry(NEXT_CONTINUATION_TOKEN, nextContinuationHandler),
      Map.entry(START_AFTER, startAfterHandler),
      Map.entry(ENCODING_TYPE, encodingHandler),
      Map.entry(OXObjectContents.elementName(), OXObjectContents::new)
    );
  }

  @Override
  public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return BTIgnoreUnrecognizedElements.IGNORE_UNRECOGNIZED_ELEMENTS;
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws Exception
  {
    this.builder = OListObjectsResponse.builder();
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final OObjectContents o -> {
        this.builder.addContents(o);
      }

      case final FieldType f -> {
        switch (f) {
          case final FieldType.FieldContinuationToken ff -> {
            this.builder.setContinuationToken(ff.value);
          }
          case final FieldType.FieldDelimiter ff -> {
            this.builder.setDelimiter(ff.value);
          }
          case final FieldType.FieldEncoding ff -> {
            this.builder.setEncoding(ff.value);
          }
          case final FieldType.FieldIsTruncated ff -> {
            this.builder.setTruncated(ff.value);
          }
          case final FieldType.FieldKeyCount ff -> {
            this.builder.setKeyCount(ff.value);
          }
          case final FieldType.FieldMaxKeys ff -> {
            this.builder.setMaxKeys(ff.value);
          }
          case final FieldType.FieldName ff -> {
            this.builder.setName(ff.value);
          }
          case final FieldType.FieldNextContinuationToken ff -> {
            this.builder.setNextContinuationToken(ff.value);
          }
          case final FieldType.FieldPrefix ff -> {
            this.builder.setPrefix(ff.value);
          }
          case final FieldType.FieldStartAfter ff -> {
            this.builder.setStartAfter(ff.value);
          }
        }
      }
      default -> {
        throw new IllegalStateException(
          "Unexpected value: %s".formatted(result)
        );
      }
    }
  }

  @Override
  public OListObjectsResponse onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
