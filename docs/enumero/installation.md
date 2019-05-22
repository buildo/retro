---
id: installation
title: Installation
---

`enumero` is composed by multiple modules:

- `enumero`: the core library
- `enumero-circe-support`: provides Circe encoder/decoders for enumero types

You can add the modules according to the needs of your project. For example:

```scala
val V = new {
  val enumero = "@ENUMERO_STABLE_VERSION@"
}

libraryDependencies ++= List(
  "io.buildo" %% "enumero" % V.enumero,
  "io.buildo" %% "enumero-circe-support" % V.enumero
)
```

In order to use `enumero` you also need to add the macro paradise plugin (for
Scala 2.12) or to enable the `-Ymacro-annotations` compiler flag (for Scala
2.13).

> 💡**PROTIP**: This is done automatically for you if you use `sbt-buildo`

```scala
// Scala 2.12
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

// Scala 2.13
scalacOptions += "-Ymacro-annotations"
```

## Snapshot versions

We publish a snapshot version on every merge on master.

The latest snapshot version is `@ENUMERO_SNAPSHOT_VERSION@` and you can use it
to try the latest unreleased features. For example:

```scala
val V = new {
  val toctoc = "@ENUMERO_SNAPSHOT_VERSION@"
}

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= List(
  "io.buildo" %% "enumero" % V.enumero,
  "io.buildo" %% "enumero-circe-support" % V.enumero
)
```
