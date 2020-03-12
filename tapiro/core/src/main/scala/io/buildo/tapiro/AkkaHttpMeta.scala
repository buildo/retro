package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.Route

import scala.meta._

object AkkaHttpMeta {
  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    controllerName: Type.Name,
    endpointsName: Term.Name,
    implicits: List[Term.Param],
    akkaHttpEndpoints: List[Defn.Val],
    routes: Term,
  ) => {
    val tapirEndpoints = q"val endpoints = $endpointsName.create[AuthToken](statusCodes)"
    q"""
    package ${`package`} {
      ..${imports.toList.map(i => q"import $i._")}
      import sttp.tapir.server.akkahttp._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode
      import akka.http.scaladsl.server._
      import akka.http.scaladsl.server.Directives._

      object $akkaHttpEndpointsName {
        def routes[AuthToken](controller: $controllerName[AuthToken], statusCodes: String => StatusCode = _ => StatusCode.UnprocessableEntity)(..$implicits): Route = {
          ..${tapirEndpoints +: akkaHttpEndpoints :+ routes}
        }
      }
    }
    """
  }

  val routes = (controllerName: Lit.String, head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    q"pathPrefix($controllerName) { List(..$rest).foldLeft[Route]($first)(_ ~ _) }"
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
        val toRoute = Term.Apply(Term.Select(endpointsName, Term.Name("toRoute")), List(content))
        q"val ${Pat.Var(name)} = $toRoute"
      }
    }
}
