import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.3",
      version      := "0.0.9",
      resolvers    += Resolver.bintrayRepo("buildo", "maven"),
      licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
    )),
    name := "toctoc",
    libraryDependencies ++=
      Seq(
        slick,
        postgresql,
        enumero,
        cats,
        scalaTest,
        bCrypt
      ).flatten
  )

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
