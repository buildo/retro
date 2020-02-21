package io.buildo.tapiro

import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{
  CaseClass,
  CaseEnum,
  Route,
  RouteSegment,
  TaggedUnion,
  Type => MetarpheusType,
}
import scala.meta._
import scala.util.control.NonFatal
import java.nio.file.Paths
import java.nio.file.Files

import cats.data.NonEmptyList

import MetarpheusHelper._

sealed trait Server
object Server {
  case object AkkaHttp extends Server
  case object Http4s extends Server
  case object NoServer extends Server
}

sealed trait TapiroRouteError
object TapiroRouteError {
  case class TaggedUnionError(taggedUnion: TaggedUnion) extends TapiroRouteError
  case class OtherError(`type`: MetarpheusType) extends TapiroRouteError
}

case class TapiroRoute(route: Route, error: TapiroRouteError)

object Util {
  import Formatter.format

  def createFiles(
    routesPaths: NonEmptyList[String],
    modelsPaths: List[String],
    outputPath: String,
    `package`: NonEmptyList[String],
    server: Server,
  ) = {
    val config = Config(Set.empty)
    val models = Metarpheus.run(modelsPaths, config).models
    val routes: List[TapiroRoute] = Metarpheus.run(routesPaths.toList, config).routes.map { route =>
      TapiroRoute(route, routeError(route, models))
    }
    val controllersRoutes =
      routes.groupBy(
        route => route.route.route.collect { case RouteSegment.String(str) => str }.head,
      )
    val modelsPackages = models.map {
      case c: CaseClass   => c.`package`
      case c: CaseEnum    => c.`package`
      case t: TaggedUnion => t.`package`
    }.collect {
      case head :: tail => NonEmptyList(head, tail)
    }
    controllersRoutes.foreach {
      case (controllerName, routes) =>
        val endpointsName = s"${controllerName}Endpoints"
        val tapirEndpoints = createTapirEndpoints(endpointsName, routes, `package`, modelsPackages)
        writeToFile(outputPath, tapirEndpoints, endpointsName)

        server match {
          case Server.Http4s =>
            val http4sEndpoints =
              createHttp4sEndpoints(
                `package`,
                controllerName,
                endpointsName,
                modelsPackages,
                routes,
              )
            http4sEndpoints.foreach(writeToFile(outputPath, _, s"${controllerName}Http4sEndpoints"))
          case Server.AkkaHttp =>
            val akkaHttpEndpoints =
              createAkkaHttpEndpoints(
                `package`,
                controllerName,
                endpointsName,
                modelsPackages,
                routes,
              )
            akkaHttpEndpoints.foreach(
              writeToFile(outputPath, _, s"${controllerName}AkkaHttpEndpoints"),
            )
          case Server.NoServer => ()
        }
    }
  }

  private[this] def createTapirEndpoints(
    endpointsName: String,
    routes: List[TapiroRoute],
    `package`: NonEmptyList[String],
    modelsPackages: List[NonEmptyList[String]],
  ): String = {
    format(
      TapirMeta.`class`(
        Meta.packageFromList(`package`),
        modelsPackages.toSet.map(Meta.packageFromList),
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
    modelsPackages: List[NonEmptyList[String]],
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
              modelsPackages.toSet.map(Meta.packageFromList),
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

  private[this] def createAkkaHttpEndpoints(
    `package`: NonEmptyList[String],
    controllerName: String,
    endpointsName: String,
    modelsPackages: List[NonEmptyList[String]],
    tapiroRoutes: List[TapiroRoute],
  ): Option[String] = {
    val routes = tapiroRoutes.map(_.route)
    routes match {
      case Nil => None
      case head :: tail =>
        Some(
          format(
            AkkaHttpMeta.`class`(
              Meta.packageFromList(`package`),
              modelsPackages.toSet.map(Meta.packageFromList),
              Type.Name(controllerName),
              Term.Name(endpointsName),
              Meta.codecsImplicits(tapiroRoutes),
              AkkaHttpMeta.endpoints(routes),
              AkkaHttpMeta.routes(head, tail),
            ),
          ),
        )
    }
  }

  private[this] def writeToFile(outputPath: String, endpoints: String, name: String): Unit = {
    val disclaimer = """
//----------------------------------------------------------
//  This code was generated by tapiro.
//  Changes to this file may cause incorrect behavior
//  and will be lost if the code is regenerated.
//----------------------------------------------------------

"""

    try {
      val endpointsPath = Paths.get(s"$outputPath/$name.scala")
      Files.createDirectories(endpointsPath.getParent)
      Files.write(endpointsPath, (disclaimer + endpoints).getBytes)

      println(s"generated tapir file ${endpointsPath.toAbsolutePath} 🤖")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

}
