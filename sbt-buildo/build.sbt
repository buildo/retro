enablePlugins(GitVersioning)

lazy val root = (project in file(".")).
  settings(
    sbtPlugin := true,
    name := "sbt-buildo",
    scalaVersion := "2.12.8",
    organization := "io.buildo",
    description := "sbt plugin for sharing configuration across projects at buildo",
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9"),
    licenses := Seq("MIT" -> url("https://github.com/buildo/sbt-buildo/blob/master/LICENSE")),
    scmInfo := Some(ScmInfo(url("https://github.com/buildo/sbt-buildo"), "git@github.com:buildo/sbt-buildo.git")),
    bintrayOrganization := Some("buildo"),
    bintrayRepository := "sbt-plugins"
  )
