import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT",
      resolvers    += Resolver.bintrayRepo("buildo", "maven")
    )),
    name := "toctoc",
    libraryDependencies ++=
      Seq(
        slick,
        postgresql,
        enumero,
        cats,
        scalaTest
      ).flatten
  )

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
