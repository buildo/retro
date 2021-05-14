---
id: installation
title: Installation
---

> wiro is deprecated.
> If you're starting a new project use [tapiro](tapiro/introduction.md) instead

## Installation

```scala
libraryDependencies ++= Seq(
  "io.buildo" %% "wiro-http-server" % "@WIRO_STABLE_VERSION@",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
```

### Snapshot releases

We publish a snapshot version on every merge on master.

The latest snapshot version is `@WIRO_SNAPSHOT_VERSION@` and you can use
it to try the latest unreleased features. For example:

```scala
libraryDependencies ++= Seq(
  "io.buildo" %% "wiro-http-server" % "@WIRO_SNAPSHOT_VERSION@",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
```
