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
import com.io7m.ophis.api.commands.OBucketList;
import com.io7m.ophis.api.commands.OOwner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.io7m.blackthorne.core.BTIgnoreUnrecognizedElements.IGNORE_UNRECOGNIZED_ELEMENTS;
import static com.io7m.ophis.vanilla.internal.xml.OQName.s3Name;

/**
 * An element handler.
 */

public final class OXListBuckets
  implements BTElementHandlerType<Object, OBucketList>
{
  private static final BTQualifiedName ELEMENT_NAME =
    s3Name("ListAllMyBucketsResult");
  private static final BTQualifiedName BUCKETS =
    s3Name("Buckets");
  private static final BTQualifiedName BUCKET =
    s3Name("Bucket");
  private static final BTQualifiedName OWNER =
    s3Name("Owner");
  private static final BTQualifiedName CONTINUATION_TOKEN =
    s3Name("ContinuationToken");

  private OOwner owner = new OOwner("", "");
  private List<OBucketDescription> buckets = List.of();
  private Optional<String> token = Optional.empty();

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

  public OXListBuckets(
    final BTElementParsingContextType context)
  {

  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final var bucketsHandler =
      Blackthorne.forListMono(
        BUCKETS,
        BUCKET,
        OXBucketDescription::new,
        IGNORE_UNRECOGNIZED_ELEMENTS
      );

    final var tokenHandler =
      Blackthorne.forScalarString(CONTINUATION_TOKEN);

    return Map.ofEntries(
      Map.entry(BUCKETS, bucketsHandler),
      Map.entry(OWNER, OXOwner::new),
      Map.entry(CONTINUATION_TOKEN, tokenHandler)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    switch (result) {
      case final List<?> r -> {
        this.buckets = (List<OBucketDescription>) List.copyOf(r);
      }
      case final OOwner r -> {
        this.owner = r;
      }
      case final String r -> {
        this.token = Optional.of(r);
      }
      default -> {

      }
    }
  }

  @Override
  public OBucketList onElementFinished(
    final BTElementParsingContextType context)
  {
    return new OBucketList(
      List.copyOf(this.buckets),
      this.owner,
      this.token
    );
  }
}
