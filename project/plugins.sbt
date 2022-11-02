addSbtPlugin("io.buildo" %% "sbt-buildo" % "0.11.10")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.22")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

resolvers += Resolver.sonatypeRepo("public")

// For CI release
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")
