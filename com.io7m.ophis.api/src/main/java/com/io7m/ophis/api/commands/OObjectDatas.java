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


package com.io7m.ophis.api.commands;

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.ophis.api.OException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class OObjectDatas
{
  private OObjectDatas()
  {

  }

  public static OObjectData ofFile(
    final Path file)
    throws OException
  {
    Objects.requireNonNull(file, "file");

    final var attributes = Map.ofEntries(
      Map.entry("File", file.toAbsolutePath().toString()),
      Map.entry("Hash Algorithm", "SHA-256")
    );

    try (final var resources = openCollection()) {
      final var baseStream =
        resources.add(Files.newInputStream(file));
      final var sha256 =
        MessageDigest.getInstance("SHA-256");
      final var sha256Stream =
        resources.add(new DigestInputStream(baseStream, sha256));
      final var md5 =
        MessageDigest.getInstance("MD5");
      final var md5Stream =
        resources.add(new DigestInputStream(sha256Stream, md5));

      final var output =
        resources.add(OutputStream.nullOutputStream());

      md5Stream.transferTo(output);

      final var hexFormat = HexFormat.of();
      return OObjectData.builder()
        .setMd5(Base64.getEncoder().encodeToString(md5.digest()))
        .setSha256(hexFormat.formatHex(sha256.digest()))
        .setSize(Files.size(file))
        .setStream(() -> {
          try {
            return Files.newInputStream(file);
          } catch (final IOException e) {
            return null;
          }
        })
        .build();
    } catch (final IOException | NoSuchAlgorithmException e) {
      throw new OException(
        e,
        "error-io",
        attributes,
        Optional.empty()
      );
    }
  }

  private static CloseableCollectionType<OException> openCollection()
  {
    return CloseableCollection.create(() -> {
      return new OException(
        "One or more resources could not be closed.",
        "error-resource",
        Map.of()
      );
    });
  }
}
