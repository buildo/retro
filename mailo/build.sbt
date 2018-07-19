val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.3"
val s3 =  "com.github.seratch" %% "awscala" % "0.5.+"
val catsCore = "org.typelevel" %% "cats-core" % "1.0.1"
val alleyCatsCore = "org.typelevel" %% "alleycats-core" % "1.0.1"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.19.0"
val circeCore = "io.circe" %% "circe-core" % "0.9.0"
val circeGeneric = "io.circe" %% "circe-generic" % "0.9.0"
val circeParser = "io.circe" %% "circe-parser" % "0.9.0"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
val scalaCacheGuava = "com.github.cb372" %% "scalacache-guava" % "0.9.3"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
val javaxMail = "javax.mail" % "javax.mail-api" % "1.6.1"
val mailin = "com.sendinblue" % "sendinblue" % "2.0"

val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo"),
  organization  := "io.buildo",
  scalaVersion  := "2.12.4",
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  releaseEarlyWith := BintrayPublisher,
  releaseEarlyNoGpg := true,
  releaseEarlyEnableSyncToMaven := false,
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
      url("https://buildo.io")),
    Developer("@calippo",
      "Claudio Caletti",
      "claudio@buildo.io",
      url("https://buildo.io"))
  ),
  homepage := Some(url("https://github.com/buildo/mailo")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/buildo/mailo"),
      "scm:git:https://github.com/buildo/mailo.git",
      Some("scm:git:git@github.com:buildo/mailo.git")
    )),
  scalacOptions := Seq(
    "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
    "-language:implicitConversions",
    "-Ypartial-unification"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("buildo", "maven"),
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
  ),
  fork in run := true
)

lazy val mailo = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "mailo",
    libraryDependencies ++= Seq(
      scalaTest,
      akkaHttp,
      s3,
      catsCore,
      alleyCatsCore,
      circeCore,
      circeGeneric,
      circeParser,
      typesafeConfig,
      akkaHttpCirce,
      scalaCacheGuava,
      scalaLogging,
      logback,
      javaxMail,
      mailin,
    )
  )

mappings in (Compile, packageBin) ~= { _.filter { n =>
  !((n._1.getName.endsWith(".conf")) || n._1.getName.endsWith(".conf.example")) }
}
