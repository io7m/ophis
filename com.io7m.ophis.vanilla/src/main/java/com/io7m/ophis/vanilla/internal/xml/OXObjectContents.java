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
import com.io7m.ophis.api.commands.OObjectContents;
import com.io7m.ophis.api.commands.OOwner;
import com.io7m.ophis.api.commands.ORestoreStatus;
import org.xml.sax.Attributes;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.io7m.ophis.vanilla.internal.xml.OQName.s3Name;

/**
 * An element handler.
 */

public final class OXObjectContents
  implements BTElementHandlerType<Object, OObjectContents>
{
  private static final BTQualifiedName ELEMENT_NAME =
    s3Name("Contents");
  private static final BTQualifiedName CHECKSUM_ALGORITHM =
    s3Name("ChecksumAlgorithm");
  private static final BTQualifiedName ETAG =
    s3Name("ETag");
  private static final BTQualifiedName KEY =
    s3Name("Key");
  private static final BTQualifiedName LAST_MODIFIED =
    s3Name("LastModified");
  private static final BTQualifiedName OWNER =
    s3Name("Owner");
  private static final BTQualifiedName RESTORE_STATUS =
    s3Name("RestoreStatus");
  private static final BTQualifiedName SIZE =
    s3Name("Size");
  private static final BTQualifiedName STORAGE_CLASS =
    s3Name("StorageClass");

  private OObjectContents.Builder builder;

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

  public OXObjectContents(
    final BTElementParsingContextType context)
  {

  }

  sealed interface FieldType
  {
    record FieldETag(String value) implements FieldType
    {
    }
    record FieldKey(String value) implements FieldType
    {
    }
    record FieldChecksumAlgorithm(String value) implements FieldType
    {

    }
    record FieldLastModified(OffsetDateTime value) implements FieldType
    {

    }
    record FieldSize(long value) implements FieldType
    {

    }
    record FieldStorageClass(String value) implements FieldType
    {

    }
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var etagHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(ETAG),
        FieldType.FieldETag::new
      );
    final var keyHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(KEY),
        FieldType.FieldKey::new
      );
    final var checksumHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(CHECKSUM_ALGORITHM),
        FieldType.FieldChecksumAlgorithm::new
      );
    final var lastModifiedHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(LAST_MODIFIED),
        s -> new FieldType.FieldLastModified(OffsetDateTime.parse(s))
      );
    final var sizeHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(SIZE),
        s -> new FieldType.FieldSize(Long.parseUnsignedLong(s))
      );
    final var storageClassHandler =
      Blackthorne.mapConstructor(
        Blackthorne.forScalarString(STORAGE_CLASS),
        FieldType.FieldStorageClass::new
      );

    return Map.ofEntries(
      Map.entry(ETAG, etagHandler),
      Map.entry(KEY, keyHandler),
      Map.entry(CHECKSUM_ALGORITHM, checksumHandler),
      Map.entry(LAST_MODIFIED, lastModifiedHandler),
      Map.entry(SIZE, sizeHandler),
      Map.entry(OWNER, OXOwner::new),
      Map.entry(STORAGE_CLASS, storageClassHandler),
      Map.entry(RESTORE_STATUS, OXRestoreStatus::new)
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
  {
    this.builder = OObjectContents.builder();
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final FieldType f -> {
        switch (f) {
          case final FieldType.FieldETag ff -> {
            this.builder.setETag(ff.value);
          }
          case final FieldType.FieldChecksumAlgorithm ff -> {
            this.builder.setChecksumAlgorithm(ff.value);
          }
          case final FieldType.FieldKey ff -> {
            this.builder.setKey(ff.value);
          }
          case final FieldType.FieldLastModified ff -> {
            this.builder.setLastModified(ff.value);
          }
          case final FieldType.FieldSize ff -> {
            this.builder.setSize(ff.value);
          }
          case final FieldType.FieldStorageClass ff -> {
            this.builder.setStorageClass(ff.value);
          }
        }
      }
      case final ORestoreStatus o -> {
        this.builder.setRestoreStatus(o);
      }
      case final OOwner o -> {
        this.builder.setOwner(o);
      }
      default -> {
        throw new IllegalStateException(
          "Unexpected value: %s".formatted(result)
        );
      }
    }
  }

  @Override
  public OObjectContents onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
