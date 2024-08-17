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


package com.io7m.ophis.tests;

import com.io7m.ervilla.api.EContainerSpec;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.api.EContainerType;
import com.io7m.ervilla.api.EPortAddressType;
import com.io7m.ervilla.api.EPortPublish;
import com.io7m.ophis.api.OClientAccessKeys;
import com.io7m.ophis.api.OClientCredentialsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.io7m.ervilla.api.EPortProtocol.TCP;

public final class OMinIOFixture
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OMinIOFixture.class);

  private final int port;
  private final int portAdmin;
  private final EContainerType container;

  private OMinIOFixture(
    final EContainerType inContainer,
    final int inPort,
    final int inPortAdmin)
  {
    this.container =
      Objects.requireNonNull(inContainer, "container");

    this.port = inPort;
    this.portAdmin = inPortAdmin;
  }

  public static String rootUser()
  {
    return "minio";
  }

  public static String rootPassword()
  {
    return "12345678";
  }

  public static String defaultBucket()
  {
    return "bucket-0";
  }

  public static OMinIOFixture create(
    final EContainerSupervisorType supervisor,
    final int port,
    final int portAdmin)
    throws Exception
  {
    final var spec =
      EContainerSpec.builder(
          "quay.io",
          "minio/minio",
          OTestProperties.MINIO_VERSION
        )
        .addPublishPort(new EPortPublish(
          new EPortAddressType.All(),
          port,
          port,
          TCP
        ))
        .addPublishPort(new EPortPublish(
          new EPortAddressType.All(),
          portAdmin,
          portAdmin,
          TCP
        ))
        .addArgument("server")
        .addArgument("/data")
        .addArgument("--console-address")
        .addArgument(":9001")
        .addEnvironmentVariable("MINIO_ROOT_USER", rootUser())
        .addEnvironmentVariable("MINIO_ROOT_PASSWORD", rootPassword())
        .setReadyCheck(new OMinIOReadyCheck(new EPortAddressType.Address4("localhost"), port));

    return new OMinIOFixture(
      supervisor.start(spec.build()),
      port,
      portAdmin
    );
  }

  public int port()
  {
    return this.port;
  }

  public void createBucket(
    final String name)
    throws Exception
  {
    LOG.debug("Creating bucket {}.", name);

    this.container.executeAndWait(
      List.of(
        "mc",
        "mb",
        "test/" + name
      ),
      10L,
      TimeUnit.SECONDS
    );

    LOG.debug("Created bucket {}.", name);
  }

  public void createUser(
    final String username,
    final String password,
    final OClientCredentialsType credentials)
    throws Exception
  {
    LOG.debug("Creating user {}.", username);

    this.container.executeAndWait(
      List.of(
        "mc",
        "admin",
        "user",
        "add",
        "test",
        username,
        password
      ),
      10L,
      TimeUnit.SECONDS
    );

    switch (credentials) {
      case final OClientAccessKeys accessKeys -> {
        this.container.executeAndWait(
          List.of(
            "mc",
            "admin",
            "user",
            "svcacct",
            "add",
            "--access-key",
            accessKeys.accessKey(),
            "--secret-key",
            accessKeys.secret(),
            "test",
            username
          ),
          10L,
          TimeUnit.SECONDS
        );
      }
    }

    this.container.executeAndWait(
      List.of(
        "mc",
        "admin",
        "policy",
        "attach",
        "test",
        "readwrite",
        "--user",
        username
      ),
      10L,
      TimeUnit.SECONDS
    );

    this.container.executeAndWait(
      List.of(
        "mc",
        "admin",
        "user",
        "info",
        "test",
        username
      ),
      10L,
      TimeUnit.SECONDS
    );

    LOG.debug("Created user {}.", username);
  }

  public void reset()
    throws Exception
  {
    LOG.debug("Resetting fixture.");

    this.container.executeAndWait(
      List.of(
        "mc",
        "alias",
        "set",
        "test",
        "http://localhost:9000",
        rootUser(),
        rootPassword()
      ),
      10L,
      TimeUnit.SECONDS
    );
  }

  public void attachUserPolicy(
    final String username,
    final String policy)
    throws Exception
  {
    this.container.executeAndWait(
      List.of(
        "mc",
        "admin",
        "policy",
        "attach",
        "test",
        policy,
        "--user",
        username
      ),
      10L,
      TimeUnit.SECONDS
    );
  }

  public void detachUserPolicy(
    final String username,
    final String policy)
    throws Exception
  {
    this.container.executeAndWait(
      List.of(
        "mc",
        "admin",
        "policy",
        "detach",
        "test",
        policy,
        "--user",
        username
      ),
      10L,
      TimeUnit.SECONDS
    );
  }
}
