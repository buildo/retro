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
    tapiro / tapiroServer := Server.AkkaHttp, //or Server.Http4s
  )
  .enablePlugins(SbtTapiro)
```

You can now run it with `sbt application/tapiro`.

```scala
## Dependencies

The generated code comes with library dependencies.

In case akka-http version is used:
```scala
val V = new {
  val circe = "0.9.1"
  val tapir = "0.12.19"
  val akkaHttp = "10.1.11"
}

val tapiroDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % V.tapir,
  "com.typesafe.akka" %% "akka-http" % V.akkaHttp,
  "io.circe" %% "circe-core" % V.circe,
)
```

In case http4s is used:

```scala
val V = new {
  val circe = "0.9.1"
  val tapir = "0.12.19"
}

val tapiroDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir,
  "io.circe" %% "circe-core" % V.circe,
)
```

These dependencies usually go under `project/Dependencies.scala`