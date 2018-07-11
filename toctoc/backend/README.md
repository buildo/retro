# toctoc

This is the backend-side of toctoc. It comes in different modules:

- `toctoc-core`: defines the abstractions
- `toctoc-wiro`: provides wiro-specific controllers
- `toctoc-slick`: provides slick-specific implementations for Postgres databases
- `toctoc-slick-mysql`: provides slick-specific implementations for MySql databases
- `toctoc-quill`: provides quill-specific implementations

## Quick start:

```scala
val V = new {
  val toctoc = "<version>"
}

libraryDependencies ++= List(
  "io.buildo" %% "toctoc-core" % V.toctoc,
  "io.buildo" %% "toctoc-wiro" % V.toctoc,
  "io.buildo" %% "toctoc-slick" % V.toctoc,
)
```
