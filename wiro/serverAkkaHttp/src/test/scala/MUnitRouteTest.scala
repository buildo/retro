package wiro

import akka.http.scaladsl.testkit.RouteTest

trait MUnitRouteTest extends RouteTest with MUnitTestFrameworkInterface { this: munit.FunSuite => }
