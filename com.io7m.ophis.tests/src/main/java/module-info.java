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

/**
 * S3 client (Test suite).
 */

open module com.io7m.ophis.tests
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires org.junit.jupiter.api;
  requires org.junit.jupiter.engine;
  requires org.junit.platform.commons;
  requires org.junit.platform.engine;
  requires org.junit.platform.launcher;

  requires com.io7m.ervilla.api;

  requires com.io7m.ophis.api;
  requires com.io7m.ophis.vanilla;
  requires org.slf4j;
  requires com.io7m.ervilla.test_extension;
  requires com.io7m.zelador.test_extension;
  requires java.net.http;

  exports com.io7m.ophis.tests;
}
