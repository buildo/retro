package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.Route

import scala.meta._

object AkkaHttpMeta {
  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    controllerName: Type.Name,
    tapirEndpointsName: Term.Name,
    httpEndpointsName: Term.Name,
    implicits: List[Term.Param],
    akkaHttpEndpoints: List[Defn.Val],
    routes: Term,
  ) => {
    val tapirEndpoints = q"val endpoints = $tapirEndpointsName.create[AuthToken](statusCodes)"
    q"""
    package ${`package`} {
      ..${imports.toList.sortWith(_.toString < _.toString).map(i => q"import $i._")}
      import akka.http.scaladsl.server._
      import akka.http.scaladsl.server.Directives._
      import io.circe.{ Decoder, Encoder }
      import sttp.tapir.server.akkahttp._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode

      object $httpEndpointsName {
        def routes[AuthToken](controller: $controllerName[AuthToken], statusCodes: String => StatusCode = _ => StatusCode.UnprocessableEntity)(..$implicits): Route = {
          ..${tapirEndpoints +: akkaHttpEndpoints :+ routes}
        }
      }
    }
    """
  }

  val routes = (pathName: Lit.String, head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    q"pathPrefix($pathName) { List(..$rest).foldLeft[Route]($first)(_ ~ _) }"
  }

  val endpoints = (routes: List[TapiroRoute]) =>
    routes.map { route =>
      val name = Term.Name(route.route.name.last)
      val endpointImpl = Meta.toEndpointImplementation(route)
      q"val ${Pat.Var(name)} = endpoints.$name.toRoute($endpointImpl)"
    }
}
