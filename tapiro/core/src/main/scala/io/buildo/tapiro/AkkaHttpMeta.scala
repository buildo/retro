package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Route, Type => MetarpheusType}

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
      ..${imports.toList.map(i => q"import $i._")}
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

  val endpoints = (routes: List[Route]) =>
    routes.map { route =>
      val name = Term.Name(route.name.last)
      val endpointsName = Term.Select(Term.Name("endpoints"), name)
      val controllersName = Term.Select(Term.Name("controller"), name)
      val controllerContent =
        route.method match {
          case "get" =>
            if (route.params.length <= 1) controllersName
            else Term.Select(Term.Eta(controllersName), Term.Name("tupled"))
          case "post" =>
            val fields = route.params
              .filterNot(_.tpe == MetarpheusType.Name("AuthToken"))
              .map(p => Term.Name(p.name.getOrElse(Meta.typeNameString(p.tpe))))
            q"x => $controllersName(..${fields.map(f => q"x.$f")})"
          case _ =>
            throw new Exception("method not supported")
        }
      val toRoute =
        Term.Apply(Term.Select(endpointsName, Term.Name("toRoute")), List(controllerContent))
      q"val ${Pat.Var(name)} = $toRoute"
    }
}
