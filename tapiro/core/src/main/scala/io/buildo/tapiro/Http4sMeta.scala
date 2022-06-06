package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.Route

import scala.meta._

object Http4sMeta {
  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    controllerName: Type.Name,
    tapirEndpointsName: Term.Name,
    authTokenName: Type.Name,
    httpEndpointsName: Term.Name,
    implicits: List[Term.Param],
    http4sEndpoints: List[Defn.Val],
    routes: Term,
  ) => {
    val authTokenTypeParam: Type.Param =
      Type.Param(List(), authTokenName, List(), Type.Bounds(None, None), List(), List())
    val tapirEndpoints = q"val endpoints = $tapirEndpointsName.create[$authTokenName](statusCodes)"
    q"""
    package ${`package`} {
      ..${imports.toList.sortWith(_.toString < _.toString).map(i => q"import $i._")}
      import cats.effect._
      import cats.implicits._
      import cats.data.NonEmptyList
      import io.circe.{ Decoder, Encoder }
      import org.http4s._
      import org.http4s.server.Router
      import sttp.tapir.server.http4s._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode

      object $httpEndpointsName {
        def routes[F[_]: Async, $authTokenTypeParam](controller: $controllerName[F, $authTokenName], statusCodes: String => StatusCode = _ => StatusCode.UnprocessableEntity)(..$implicits): HttpRoutes[F] = {
          ..${tapirEndpoints +: http4sEndpoints :+ routes}
        }
      }
    }
    """
  }

  val routes = (pathName: Lit.String, head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    val route: Lit.String = Lit.String("/" + pathName.value)
    q"Router($route -> NonEmptyList.of($first, ..$rest).reduceK)"
  }

  val endpoints = (routes: List[TapiroRoute], authTokenName: String) =>
    routes.map { route =>
      val name = Term.Name(route.route.name.last)
      val endpointImpl = Meta.toEndpointImplementation(route, authTokenName)
      q"val ${Pat.Var(name)} = Http4sServerInterpreter[F]().toRoutes(endpoints.$name.serverLogic($endpointImpl))"
    }
}
