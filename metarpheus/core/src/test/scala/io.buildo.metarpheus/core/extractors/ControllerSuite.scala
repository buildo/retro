package io.buildo.metarpheus
package core
package test

import org.scalatest._

import extractors._

class ControllerSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    Fixtures.controllers.parse[Source].get
  }

  test("parse successfully") {
    parsed
  }

  test("extract routes from fixture code") {
    import intermediate._

    val result = controller.extractAllRoutes(parsed)

    assert(
      result.toString ===
        List(
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("getByCoolnessAndSize"),
            ),
            params = List(
              RouteParam(
                Some("coolness"),
                Type.Name("String"),
                true,
                Some("how cool it is"),
              ),
              RouteParam(
                Some("size"),
                Type.Name("Int"),
                false,
                Some("the number of tents"),
              ),
              RouteParam(
                Some("nickname"),
                Type.Name("String"),
                true,
                Some("a friendly name for the camping"),
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Apply("List", List(Type.Name("Camping"))),
            error = Some(Type.Name("String")),
            desc = Some("get campings matching the requested coolness and size"),
            name = List("campingController", "getByCoolnessAndSize"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("getBySizeAndDistance"),
            ),
            params = List(
              RouteParam(
                Some("size"),
                Type.Name("Int"),
                true,
                Some("the number of tents"),
              ),
              RouteParam(
                Some("distance"),
                Type.Name("Int"),
                true,
                Some("how distant it is"),
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Apply("List", List(Type.Name("Camping"))),
            error = Some(Type.Name("String")),
            desc = Some("get campings matching the requested size and distance"),
            name = List("campingController", "getBySizeAndDistance"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("getById"),
            ),
            params = List(
              RouteParam(
                Some("id"),
                Type.Name("Int"),
                true,
                Some("camping id"),
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = true,
            returns = Type.Name("Camping"),
            error = Some(Type.Name("String")),
            desc = Some("get a camping by id"),
            name = List("campingController", "getById"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("getByTypedId"),
            ),
            params = List(
              RouteParam(
                Some("id"),
                Type.Apply("Id", Seq(Type.Name("Camping"))),
                true,
                None,
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = true,
            returns = Type.Name("Camping"),
            error = Some(Type.Name("String")),
            desc = Some("get a camping by typed id"),
            name = List("campingController", "getByTypedId"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("getByHasBeach"),
            ),
            params = List(
              RouteParam(
                Some("hasBeach"),
                Type.Name("Boolean"),
                true,
                Some("whether there's a beach"),
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Apply("List", List(Type.Name("Camping"))),
            error = Some(Type.Name("String")),
            desc = Some("get campings based on whether they're close to a beach"),
            name = List("campingController", "getByHasBeach"),
          ),
          Route(
            method = "post",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("create"),
            ),
            params = List(
              RouteParam(
                Some("camping"),
                Type.Name("Camping"),
                true,
                None,
                inBody = true,
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Name("Camping"),
            error = Some(Type.Name("CreateCampingError")),
            desc = Some("create a camping"),
            name = List("campingController", "create"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("taglessFinalRouteV1"),
            ),
            params = List(
              RouteParam(
                Some("input"),
                Type.Name("String"),
                true,
                None,
                inBody = false,
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Name("String"),
            error = None,
            desc = None,
            name = List("campingController", "taglessFinalRouteV1"),
          ),
          Route(
            method = "get",
            route = List(
              RouteSegment.String("campings"),
              RouteSegment.String("taglessFinalRouteV2"),
            ),
            params = List(
              RouteParam(
                Some("input"),
                Type.Name("String"),
                true,
                None,
                inBody = false,
              ),
            ),
            pathName = Some("campings"),
            controllerType = Type.Apply("CampingController", List()),
            authenticated = false,
            returns = Type.Name("String"),
            error = Some(Type.Name("Exception")),
            desc = None,
            name = List("campingController", "taglessFinalRouteV2"),
          ),
        ).toString,
    )

  }
}
