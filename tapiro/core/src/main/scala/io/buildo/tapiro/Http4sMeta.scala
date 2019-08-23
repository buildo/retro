package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.Route

import scala.meta._

object Http4sMeta {
  val `class` = (
    `package`: Term.Name,
    controllerName: Type.Name,
    endpointsName: Type.Name,
    implicits: List[Term.Param],
    http4sEndpoints: List[Defn.Val],
    app: Defn.Val,
  ) => {
    val tapirEndpoints = q"private[this] val endpoints = new $endpointsName()"
    val httpsEndpointsName = Type.Name(s"${controllerName.syntax}Http4sEndpoints")
    q"""package ${`package`} {
  import cats.effect._
  import cats.implicits._
  import cats.data.NonEmptyList

  import org.http4s._
  import org.http4s.implicits._

  import tapir.server.http4s._
  import tapir.Codec.JsonCodec

  class $httpsEndpointsName(controller: $controllerName[IO])(..$implicits) {
    ..${tapirEndpoints +: http4sEndpoints :+ app}
  }
}
"""
  }

  val app = (head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    q"val app: HttpApp[IO] = NonEmptyList($first, List(..$rest)).reduceK.orNotFound"
  }

  val endpoints = (routes: List[Route]) =>
    routes.flatMap { route =>
      val name = Term.Name(route.name.last)
      val endpointsName = Term.Select(Term.Name("endpoints"), name)
      val controllersName = Term.Select(Term.Name("controller"), name)
      val controllerContent =
        if (route.method == "get") Some(Term.Select(Term.Eta(controllersName), Term.Name("tupled")))
        else if (route.method == "post") Some(controllersName)
        else None
      controllerContent.map { content =>
        val toRoutes = Term.Apply(Term.Select(endpointsName, Term.Name("toRoutes")), List(content))
        q"private[this] val ${Pat.Var(name)}: HttpRoutes[IO] = $toRoutes"
      }
    }
}
