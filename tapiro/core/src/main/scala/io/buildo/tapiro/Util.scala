package io.buildo.tapiro

import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{Route, RouteSegment, TaggedUnion}
import scala.meta._
import scala.util.control.NonFatal
import java.nio.file.Paths
import java.nio.file.Files

import cats.data.NonEmptyList

import MetarpheusHelper._

case class TapiroRoute(route: Route, errorValues: List[TaggedUnion.Member])

object Util {
  import Formatter.format

  def createFiles(from: String, to: String, `package`: NonEmptyList[String], includeHttp4sModels: Boolean) = {
    val meta = Metarpheus.run(List(from), Config(Set.empty))
    val routes: List[TapiroRoute] = meta.routes.map { route =>
      val errorValues: List[TaggedUnion.Member] = routeErrorValues(route, meta.models)
      TapiroRoute(route, errorValues)
    }
    val controllersRoutes =
      routes.groupBy(
        route => route.route.route.collect { case RouteSegment.String(str) => str }.head,
      )
    controllersRoutes.foreach {
      case (controllerName, routes) =>
        val endpointsName = s"${controllerName}Endpoints"
        val tapirEndpoints = createTapirEndpoints(endpointsName, routes, `package`)
        writeToFile(to, tapirEndpoints, endpointsName)

        if (includeHttp4sModels) {
          val http4sEndpoints =
            createHttp4sEndpoints(`package`, controllerName, endpointsName, routes)
          http4sEndpoints.foreach(writeToFile(to, _, s"${controllerName}Http4sEndpoints"))
        }
    }
  }

  private[this] def createTapirEndpoints(
    endpointsName: String,
    routes: List[TapiroRoute],
    `package`: NonEmptyList[String],
  ): String = {
    format(
      TapirMeta.`class`(
        Meta.packageFromList(`package`),
        Term.Name(endpointsName),
        Meta.codecsImplicits(routes),
        routes.map(TapirMeta.routeToTapirEndpoint),
      ),
    )
  }
  private[this] def createHttp4sEndpoints(
    `package`: NonEmptyList[String],
    controllerName: String,
    endpointsName: String,
    tapiroRoutes: List[TapiroRoute],
  ): Option[String] = {
    val routes = tapiroRoutes.map(_.route)
    routes match {
      case Nil => None
      case head :: tail =>
        Some(
          format(
            Http4sMeta.`class`(
              Meta.packageFromList(`package`),
              Type.Name(controllerName),
              Term.Name(endpointsName),
              Meta.codecsImplicits(tapiroRoutes) :+ param"implicit cs: ContextShift[F]",
              Http4sMeta.endpoints(routes),
              Http4sMeta.routes(head, tail),
            ),
          ),
        )
    }
  }

  private[this] def writeToFile(to: String, endpoints: String, name: String): Unit = {
    try {
      val endpointsPath = Paths.get(s"$to/$name.scala")
      Files.createDirectories(endpointsPath.getParent)
      Files.write(endpointsPath, endpoints.getBytes)

      println(s"generated tapir file ${endpointsPath.toAbsolutePath} 🤖")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

}
