package io.buildo.metarpheus
package core
package test

import org.scalatest._

import extractors._

class ApiSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    List(
      Fixtures.models.parse[Source].get,
      Fixtures.controllers.parse[Source].get,
    )
  }

  test("extract used models") {
    import intermediate._

    val api = extractFullAPI(parsed).stripUnusedModels(Common.modelsForciblyInUse)

    assert(api.routes.collectFirst {
      // format: off
      case Route(_, List(
        RouteSegment.String("campings"),
        RouteSegment.String("getByCoolnessAndSize"),
      ), _, _, _, _, _, _, _, _) => ()
      // format: on
    }.isDefined)

    assert(api.models.collectFirst {
      case TaggedUnion(
          "CreateCampingError",
          _,
          _,
          _,
          ) =>
        ()
    }.isDefined)

    assert(api.models.collectFirst {
      case CaseClass(
          "IgnoreMe",
          _,
          _,
          _,
          _,
          _,
          ) =>
        ()
    }.isEmpty)
  }

  test("extract used models, discarding error models") {
    import intermediate._

    val api = extractFullAPI(parsed)
      .stripUnusedModels(Common.modelsForciblyInUse, discardRouteErrorModels = true)

    assert(api.models.collectFirst {
      case TaggedUnion(
          "CreateCampingError",
          _,
          _,
          _,
          ) =>
        ()
    }.isEmpty)

    assert(api.models.collectFirst {
      case CaseClass(
          "Camping",
          _,
          _,
          _,
          _,
          _,
          ) =>
        ()
    }.isDefined)
  }
}
