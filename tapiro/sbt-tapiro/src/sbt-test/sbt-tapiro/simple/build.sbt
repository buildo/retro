val circeVersion = "0.10.0"
val http4sVersion = "0.20.0-M7"

lazy val root = (project in file("."))
  .enablePlugins(SbtTapiro)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "1.2.0",
      "com.softwaremill.tapir" %% "tapir-json-circe" % "0.5.2",
      "com.softwaremill.tapir" %% "tapir-http4s-server" % "0.5.2",
      "com.softwaremill.tapir" %% "tapir-openapi-docs" % "0.5.2",
      "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % "0.5.2",
      "com.softwaremill.tapir" %% "tapir-sttp-client" % "0.5.2",
      "com.softwaremill.tapir" %% "tapir-core" % "0.5.2",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion),
    tapiro / tapiroEndpointsPath := "endpoints",
    tapiro / tapiroEndpointsPackage := NonEmptyList("endpoints", Nil),
  )
