import Dependencies._

inThisBuild(
  List(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala211, scala212),
    resolvers += Resolver.bintrayRepo("buildo", "maven"),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    developers := List(
      Developer("@danielegallingani",
                "Daniele Gallingani",
                "daniele@buildo.io",
                url("https://buildo.io")),
      Developer("@bytecodeguru",
                "Giuseppe Moscarella",
                "giuseppe.moscarella@buildo.io",
                url("https://buildo.io")),
      Developer("@gabro",
                "Gabriele Petronella",
                "gabriele@buildo.io",
                url("https://buildo.io"))
    ),
    homepage := Some(url("https://github.com/buildo/toctoc")),
    releaseEarlyWith := BintrayPublisher,
    releaseEarlyNoGpg := true,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/buildo/toctoc"),
        "scm:git:https://github.com/buildo/toctoc.git",
        Some("scm:git:git@github.com:buildo/toctoc.git")
      ))
  ))

lazy val core = project
  .settings(
    name := "toctoc-core",
    libraryDependencies ++= coreDependencies
  )

lazy val slick = project
  .settings(
    name := "toctoc-slick",
    libraryDependencies ++= slickDependencies
  )
  .dependsOn(core)

lazy val quill = project
  .settings(
    name := "toctoc-quill",
    libraryDependencies ++= quillDependencies
  )
  .dependsOn(core)

lazy val wiro = project
  .settings(
    name := "toctoc-wiro",
    libraryDependencies ++= wiroDependencies
  )
  .dependsOn(core)
