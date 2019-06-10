package io.buildo.tapiro

import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{Route, RouteSegment}
import scala.meta._
import scala.util.control.NonFatal
import java.nio.file.Paths
import java.nio.file.Files

object Util {
  import Formatter.format

  def createFiles(from: String, to: String, `package`: String, includeHttp4sModels: Boolean) = {
    val routes: List[Route] = Metarpheus.run(List(from), Config(Set.empty)).routes
    val controllersRoutes =
      routes.groupBy(route => route.route.collect { case RouteSegment.String(str) => str }.head)
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
    routes: List[Route],
    `package`: String,
  ): String = format(
    Meta.tapirClass(
      Term.Name(`package`),
      Type.Name(endpointsName),
      Meta.codecsImplicits(routes),
      routes.map(Meta.routeToTapirEndpoint),
    ),
  )

  private[this] def createHttp4sEndpoints(
    `package`: String,
    controllerName: String,
    endpointsName: String,
    routes: List[Route],
  ): Option[String] = {
    routes match {
      case Nil => None
      case head :: tail =>
        Some(format(
          Meta.http4sClass(
            Term.Name(`package`),
            Type.Name(controllerName),
            Type.Name(endpointsName),
            Meta.codecsImplicits(routes) :+ param"implicit io: ContextShift[IO]",
            Meta.http4sEndpoints(routes),
            Meta.httpApp(head, tail),
          ),
        ))
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
