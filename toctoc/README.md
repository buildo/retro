# toctoc

Modules:

- `toctoc-core`: defines the abstractions
- `toctoc-slick`: provides slick-specific implementations for Postgres databases
- `toctoc-slick-mysql`: provides slick-specific implementations for MySql
  databases
- `toctoc-quill`: provides quill-specific implementations

## Quick start:

```scala
val V = new {
  val toctoc = "<version>"
}

libraryDependencies ++= List(
  "io.buildo" %% "toctoc-core" % V.toctoc,
  "io.buildo" %% "toctoc-slick" % V.toctoc,
)
```
