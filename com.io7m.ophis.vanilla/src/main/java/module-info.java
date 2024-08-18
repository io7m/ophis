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

import com.io7m.ophis.vanilla.internal.commands.OClientCommandFactoryType;
import com.io7m.ophis.vanilla.internal.commands.OCmdListBucketsF;
import com.io7m.ophis.vanilla.internal.commands.OCmdListObjectsF;
import com.io7m.ophis.vanilla.internal.commands.OCmdPutObjectF;

/**
 * S3 client (Vanilla client implementation).
 */

module com.io7m.ophis.vanilla
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.ophis.api;

  requires com.io7m.blackthorne.core;
  requires com.io7m.jlexing.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.jxe.core;
  requires java.net.http;

  uses OClientCommandFactoryType;

  provides OClientCommandFactoryType
    with OCmdListBucketsF,
      OCmdListObjectsF,
      OCmdPutObjectF;

  exports com.io7m.ophis.vanilla;

  exports com.io7m.ophis.vanilla.internal
    to com.io7m.ophis.tests;
  exports com.io7m.ophis.vanilla.internal.commands
    to com.io7m.ophis.tests;
  exports com.io7m.ophis.vanilla.internal.xml
    to com.io7m.ophis.tests;
}
