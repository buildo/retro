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
- `toctoc-quill`: provides quill-specific implementations

You can cherry-pick the modules according to the needs of your project. For
example:

```scala
val V = new {
  val toctoc = "@STABLE_VERSION@"
}

libraryDependencies ++= List(
  "io.buildo" %% "toctoc-core" % V.toctoc,
  "io.buildo" %% "toctoc-slick-postgresql" % V.toctoc
)
```
