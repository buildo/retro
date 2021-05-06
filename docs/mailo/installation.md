---
id: installation
title: Installation
---

You can add install mailo like so:

```scala
libraryDependencies += "io.buildo" %% "mailo" % "@MAILO_STABLE_VERSION@"
```

## Snapshot versions

We publish a snapshot version on every merge on master.

The latest snapshot version is `@MAILO_SNAPSHOT_VERSION@` and you can use it
to try the latest unreleased features. For example:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= "io.buildo" %% "mailo" % "@MAILO_SNAPSHOT_VERSION@"
```
