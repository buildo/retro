package io.buildo.tapiro

import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{
  CaseClass,
  CaseEnum,
  Route,
  TaggedUnion,
  Type => MetarpheusType,
}
import scala.meta._
import scala.util.control.NonFatal
import java.nio.file.Paths
import java.nio.file.Files
import cats.data.NonEmptyList
import MetarpheusHelper._
import org.apache.logging.log4j.LogManager

import Meta.typeNameString

sealed trait Server
object Server {
  case object AkkaHttp extends Server
  case object Http4s extends Server
  case object NoServer extends Server
}

sealed trait RouteError
object RouteError {
  case class TaggedUnionError(taggedUnion: TaggedUnion) extends RouteError
  case class OtherError(`type`: MetarpheusType) extends RouteError
}

sealed trait RouteMethod
object RouteMethod {
  case object GET extends RouteMethod
  case object POST extends RouteMethod
}

case class TapiroRoute(route: Route, method: RouteMethod, error: RouteError)

class Util() {
  import Formatter.format

  val logger = LogManager.getLogger("io.buildo.tapiro")

  def createFiles(
    routesPaths: List[String],
    modelsPaths: List[String],
    outputPath: String,
    `package`: List[String],
    server: Server,
  ) = {
    NonEmptyList.fromList(`package`) match {
      case Some(nonEmptyPackage) =>
        val config = Config(Set.empty)
        val models = Metarpheus.run(modelsPaths, config).models
        val routes: List[TapiroRoute] =
          Metarpheus.run(routesPaths, config).routes.map(toTapiroRoute(models))
        val controllersRoutes =
          routes.groupBy(route => (route.route.controllerType, route.route.pathName))
        val modelsPackages = models.map {
          case c: CaseClass   => c.`package`
          case c: CaseEnum    => c.`package`
          case t: TaggedUnion => t.`package`
        }.collect { case head :: tail =>
          NonEmptyList(head, tail)
        }
        controllersRoutes.foreach { case ((controllerType, pathName), routes) =>
          val controllerName = typeNameString(controllerType)
          val pathNameOrController = pathName.getOrElse(controllerName)
          val tapirEndpointsName = s"${pathNameOrController}TapirEndpoints".capitalize
          val httpEndpointsName = s"${pathNameOrController}HttpEndpoints".capitalize
          val tapirEndpoints =
            createTapirEndpoints(tapirEndpointsName, routes, nonEmptyPackage, modelsPackages)
          writeToFile(outputPath, tapirEndpoints, tapirEndpointsName)

          val routesPackages = routes
            .map(_.route.controllerPackage)
            .collect { case head :: tail =>
              NonEmptyList(head, tail)
            }
          server match {
            case Server.Http4s =>
              val http4sEndpoints =
                createHttp4sEndpoints(
                  nonEmptyPackage,
                  pathNameOrController,
                  controllerName,
                  tapirEndpointsName,
                  httpEndpointsName,
                  modelsPackages ++ routesPackages,
                  routes,
                )
              http4sEndpoints.foreach(writeToFile(outputPath, _, httpEndpointsName))
            case Server.AkkaHttp =>
              val akkaHttpEndpoints =
                createAkkaHttpEndpoints(
                  nonEmptyPackage,
                  pathNameOrController,
                  controllerName,
                  tapirEndpointsName,
                  httpEndpointsName,
                  modelsPackages ++ routesPackages,
                  routes,
                )
              akkaHttpEndpoints.foreach(
                writeToFile(outputPath, _, httpEndpointsName),
              )
            case Server.NoServer => ()
          }
        }
      case None => logger.error("please provide a package to tapiro")
    }
  }

  private[this] def createTapirEndpoints(
    tapirEndpointsName: String,
    routes: List[TapiroRoute],
    `package`: NonEmptyList[String],
    requiredPackages: List[NonEmptyList[String]],
  ): String = {
    format(
      TapirMeta.`class`(
        Meta.packageFromList(`package`),
        requiredPackages.toSet.map(Meta.packageFromList),
        Term.Name(tapirEndpointsName),
        Meta.codecsImplicits(routes),
        routes.map(TapirMeta.routeToTapirEndpoint),
        routes.flatMap(TapirMeta.routeClassDeclarations),
        routes.flatMap(TapirMeta.routeCodecDeclarations),
      ),
    )
  }

  private[this] def createHttp4sEndpoints(
    `package`: NonEmptyList[String],
    pathName: String,
    controllerName: String,
    tapirEndpointsName: String,
    httpEndpointsName: String,
    requiredPackages: List[NonEmptyList[String]],
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
              requiredPackages.toSet.map(Meta.packageFromList),
              Type.Name(controllerName),
              Term.Name(tapirEndpointsName),
              Term.Name(httpEndpointsName),
              Meta.codecsImplicits(tapiroRoutes) :+ param"implicit cs: ContextShift[F]",
              Http4sMeta.endpoints(tapiroRoutes),
              Http4sMeta.routes(Lit.String(pathName), head, tail),
            ),
          ),
        )
    }
  }

  private[this] def createAkkaHttpEndpoints(
    `package`: NonEmptyList[String],
    pathName: String,
    controllerName: String,
    tapirEndpointsName: String,
    httpEndpointsName: String,
    requiredPackages: List[NonEmptyList[String]],
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
              requiredPackages.toSet.map(Meta.packageFromList),
              Type.Name(controllerName),
              Term.Name(tapirEndpointsName),
              Term.Name(httpEndpointsName),
              Meta.codecsImplicits(tapiroRoutes),
              AkkaHttpMeta.endpoints(tapiroRoutes),
              AkkaHttpMeta.routes(Lit.String(pathName), head, tail),
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

      logger.info(s"generated tapir file ${endpointsPath.toAbsolutePath} 🤖")
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
  }

}
