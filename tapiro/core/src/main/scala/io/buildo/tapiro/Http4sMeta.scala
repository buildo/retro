package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.Route

import scala.meta._

object Http4sMeta {
  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    controllerName: Type.Name,
    endpointsName: Term.Name,
    implicits: List[Term.Param],
    http4sEndpoints: List[Defn.Val],
    routes: Term,
  ) => {
    val tapirEndpoints = q"val endpoints = $endpointsName.create[AuthToken](statusCodes)"
    q"""
    package ${`package`} {
      ..${imports.toList.map(i => q"import $i._")}
      import cats.effect._
      import cats.implicits._
      import cats.data.NonEmptyList
      import org.http4s._
      import org.http4s.server.Router
      import sttp.tapir.server.http4s._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode

      object $httpsEndpointsName {
        def routes[F[_]: Sync, AuthToken](controller: $controllerName[F, AuthToken], statusCodes: String => StatusCode = _ => StatusCode.UnprocessableEntity)(..$implicits): HttpRoutes[F] = {
          ..${tapirEndpoints +: http4sEndpoints :+ routes}
        }
      }
    }
    """
  }

  val routes = (controllerName: Lit.String, head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    val route: Lit.String = Lit.String("/" + controllerName.value)
    q"Router($route -> NonEmptyList($first, List(..$rest)).reduceK)"
  }

  val endpoints = (routes: List[Route]) =>
    routes.flatMap { route =>
      val name = Term.Name(route.name.last)
      val endpointsName = Term.Select(Term.Name("endpoints"), name)
      val controllersName = Term.Select(Term.Name("controller"), name)
      val controllerContent =
        if (route.params.length <= 1) Some(controllersName)
        else Some(Term.Select(Term.Eta(controllersName), Term.Name("tupled")))
      controllerContent.map { content =>
        val toRoutes = Term.Apply(Term.Select(endpointsName, Term.Name("toRoutes")), List(content))
        q"val ${Pat.Var(name)} = $toRoutes"
      }
    }
}
