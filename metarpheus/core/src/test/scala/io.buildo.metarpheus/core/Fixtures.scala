package io.buildo.metarpheus
package core
package test

import scala.io.Source

object Fixtures {
  val models = Source.fromFile("metarpheus/core/src/test/resources/fixtures/models.scala").mkString
  val controllers =
    Source.fromFile("metarpheus/core/src/test/resources/fixtures/controllers.scala").mkString
}
