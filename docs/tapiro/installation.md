---
id: installation
title: Installation
---

`tapiro` can be installed as an Sbt plugin.

`sbt-tapiro` is an Sbt plugin that uses `tapiro` to generate http/json routes parsing scala traits definitions.

## Installation

To start using `sbt-tapiro` simply add this line in `project/plugins.sbt`

```scala
addSbtPlugin("io.buildo" %% "sbt-tapiro" % "@SBT_TAPIRO_STABLE_VERSION@")
```

### Snapshot releases

We publish a snapshot version on every merge on master.

The latest snapshot version is `@SBT_TAPIRO_SNAPSHOT_VERSION@` and you can use
it to try the latest unreleased features. For example:

```scala
addSbtPlugin("io.buildo" %% "sbt-tapiro" % "@SBT_TAPIRO_SNAPSHOT_VERSION@")
resolvers += Resolver.sonatypeRepo("snapshots")
```

## Plugin

To use the code generator, you need to add this to your `build.sbt`.

```scala
lazy val application = project
  .settings(
    libraryDependencies ++= applicationDependencies ++ tapiroDependencies,
    tapiro / tapiroRoutesPaths := List("[path to routes]"),
    tapiro / tapiroModelsPaths := List("[path to models]"),
    tapiro / tapiroOutputPath := "[path to endpoints]",
    tapiro / tapiroEndpointsPackages := List("[package]", "[subpackage]"),
  )
  .enablePlugins(SbtTapiro)
```

You can now run it with `sbt application/tapiro`.

## Dependencies

The generated code comes with library dependencies.

```scala
val V = new {
  val circe = "@CIRCE_VERSION@"
  val tapir = "@TAPIR_VERSION@"
}

val tapiroDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir,
  "io.circe" %% "circe-core" % V.circe,
)
```

These dependencies usually go under `project/Dependencies.scala`.
