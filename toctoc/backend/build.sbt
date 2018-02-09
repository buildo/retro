import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := scala212,
      crossScalaVersions := Seq(scala211, scala212),
      resolvers    += Resolver.bintrayRepo("buildo", "maven"),
      licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
      developers := List(
        Developer("@danielegallingani", "Daniele Gallingani", "daniele@buildo.io", url("https://buildo.io")),
        Developer("@bytecodeguru", "Giuseppe Moscarella", "giuseppe.moscarella@buildo.io", url("https://buildo.io"))
      ),
      homepage := Some(url("https://github.com/buildo/toctoc")),
      releaseEarlyWith := BintrayPublisher,
      releaseEarlyRemoteSignature := true
      scmInfo := Some(ScmInfo(
        url("https://github.com/buildo/toctoc"),
        "scm:git:https://github.com/buildo/toctoc.git",
        Some("scm:git:git@github.com:buildo/toctoc.git")
      ))
    )),
    name := "toctoc",
    libraryDependencies ++=
      Seq(
        slick,
        postgresql,
        enumero,
        cats,
        scalaTest,
        bCrypt,
        wiroServer
      ).flatten
  )

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
