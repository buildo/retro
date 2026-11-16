---
id: installation
title: Installation
---

`toctoc` is composed by multiple modules:

- `toctoc-core`: defines the basic abstractions
- `toctoc-slick-postgresql`: provides slick-specific implementations for
  Postgres databases
- `toctoc-slick-mysql`: provides slick-specific implementations for MySql
  databases
- `toctoc-ldap`: provides an LDAP implementation of the login domain
- `toctoc-circe`: provides Circe encoders/decoders for core toctoc types

All modules are published for Scala 2.12, 2.13 and 3. The Scala 3 artifacts are
built with Scala 3.3, so they need Scala 3.3 or later.

The Slick modules (`toctoc-slick-postgresql` and `toctoc-slick-mysql`) depend on
Slick 3.5, which uses HikariCP 5: they need Java 11 or later at runtime, and a
project that uses them must be on Slick 3.5 too. `toctoc-core`, `toctoc-circe`
and `toctoc-ldap` still run on Java 8.

You can cherry-pick the modules according to the needs of your project. For
example:

```scala
val V = new {
  val toctoc = "@TOCTOC_STABLE_VERSION@"
}

libraryDependencies ++= List(
  "io.buildo" %% "toctoc-core" % V.toctoc,
  "io.buildo" %% "toctoc-slick-postgresql" % V.toctoc
)
```

## Snapshot versions

We publish a snapshot version on every merge on master.

The latest snapshot version is `@TOCTOC_SNAPSHOT_VERSION@` and you can use it to
try the latest unreleased features. For example:

```scala
val V = new {
  val toctoc = "@TOCTOC_SNAPSHOT_VERSION@"
}

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= List(
  "io.buildo" %% "toctoc-core" % V.toctoc,
  "io.buildo" %% "toctoc-slick-postgresql" % V.toctoc
)
```
