addSbtPlugin("io.buildo" %% "sbt-buildo" % "0.11.5")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.19")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.1.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.17.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

resolvers += Resolver.sonatypeRepo("public")

// For CI release
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0-M2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
