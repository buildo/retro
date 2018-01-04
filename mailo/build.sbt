val scalaTest = "org.scalatest" %% "scalatest" % "2.1.3" % "test"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.3"
val s3 =  "com.github.seratch" %% "awscala" % "0.5.+"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.0"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.16.0"
val circeCore = "io.circe" %% "circe-core" % "0.8.0"
val circeGeneric = "io.circe" %% "circe-generic" % "0.8.0"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
val scalaCacheGuava = "com.github.cb372" %% "scalacache-guava" % "0.9.1"

val commonSettings = Seq(
  bintrayOrganization := Some("buildo"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization  := "io.buildo",
  version       := "0.2.0",
  scalaVersion  := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalacOptions := Seq(
    "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
    "-language:implicitConversions"
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
      scalaz,
      circeCore,
      circeGeneric,
      typesafeConfig,
      akkaHttpCirce,
      scalaCacheGuava
    )
  )

mappings in (Compile, packageBin) ~= { _.filter { n =>
  !((n._1.getName.endsWith(".conf")) || n._1.getName.endsWith(".conf.example")) }
}
