---
id: introduction
title: Introduction
---

`sbt-buildo` is an Sbt plugin that's used to share common build configurations
and sbt plugins across the various projects at buildo.

## Installation

To start using `sbt-buildo` simply add this line in `project/plugins.sbt`

```scala
addSbtPlugin("io.buildo" %% "sbt-buildo" % "@SBT_BUILDO_STABLE_VERSION@")
```

### Snapshot releases

We publish a snapshot version on every merge on master.

The latest snapshot version is `@SBT_BUILDO_SNAPSHOT_VERSION@` and you can use
it to try the latest unreleased features. For example:

```scala
addSbtPlugin("io.buildo" %% "sbt-buildo" % "@SBT_BUILDO_SNAPSHOT_VERSION@")
resolvers += Resolver.sonatypeRepo("snapshots")
```
