package io.buildo.tapiro
import io.buildo.metarpheus.core.extractors._
import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{
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
import _root_.io.buildo.metarpheus.core.intermediate.RouteParam

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
        //this is needed because Metarpheus removes auth from params when authentication type is "Auth"
        //see https://github.com/buildo/retro/blob/dfe62fa54d4f34c1861d694ac0cd8fa82f0a8703/metarpheus/core/src/main/scala/io.buildo.metarpheus/core/extractors/controller.scala#L35
        val routesWithAuthParams: List[Route] = Metarpheus
          .run(routesPaths, config)
          .routes
          .map(r => {
            if (r.authenticated)
              r.copy(
                params = r.params :+ RouteParam(
                  Some("token"),
                  MetarpheusType.Name("Auth"),
                  true,
                  None,
                  true,
                ),
              )
            else r,
          })
       val routes: List[TapiroRoute] =
          routesWithAuthParams.map(toTapiroRoute(models))
       val imports = Metarpheus.parseFiles(routesPaths).map(extractImports).flatten.map(_.importers).flatten.toSet
        val controllersRoutes =
          routes.groupBy(
            route => (route.route.controllerType, route.route.pathName),
          )
        controllersRoutes.foreach {
          case ((controllerType, pathName), routes) =>
            val controllerName = typeNameString(controllerType)
            val authTypeString = Meta.authTypeString(controllerType).getOrElse("AuthToken")
            val pathNameOrController = pathName.getOrElse(controllerName)
            val tapirEndpointsName = s"${pathNameOrController}TapirEndpoints".capitalize
            val httpEndpointsName = s"${pathNameOrController}HttpEndpoints".capitalize
            val controllerImports = Meta.controllerImports(routes,imports)
            val tapirEndpoints =
              createTapirEndpoints(
                tapirEndpointsName,
                authTypeString,
                routes,
                nonEmptyPackage,
                controllerImports,             
              )
            writeToFile(outputPath, tapirEndpoints, tapirEndpointsName)
            server match {
              case Server.Http4s =>
                val http4sEndpoints =
                  createHttp4sEndpoints(
                    nonEmptyPackage,
                    pathNameOrController,
                    controllerName,
                    tapirEndpointsName,
                    authTypeString,
                    httpEndpointsName,
                    controllerImports,
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
                    authTypeString,
                    httpEndpointsName,
                    controllerImports,
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
    authTokenName: String,
    routes: List[TapiroRoute],
    `package`: NonEmptyList[String],
    requiredPackages: Set[Import],
  ): String = {
    format(
      TapirMeta.`class`(
        Meta.packageFromList(`package`),
        requiredPackages,
        Term.Name(tapirEndpointsName),
        Type.Name(authTokenName),
        Meta.codecsImplicits(routes, authTokenName),
        routes.map(TapirMeta.routeToTapirEndpoint(Term.Name(tapirEndpointsName), authTokenName)),
        routes.flatMap(r => TapirMeta.routeClassDeclarations(r, authTokenName)),
        routes.flatMap(TapirMeta.routeCodecDeclarations),
      ),
    )
  }

  private[this] def createHttp4sEndpoints(
    `package`: NonEmptyList[String],
    pathName: String,
    controllerName: String,
    tapirEndpointsName: String,
    authTokenName: String,
    httpEndpointsName: String,
    requiredPackages: Set[Import],
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
              requiredPackages,
              Type.Name(controllerName),
              Term.Name(tapirEndpointsName),
              Type.Name(authTokenName),
              Term.Name(httpEndpointsName),
              Meta
                .codecsImplicits(tapiroRoutes, authTokenName) :+ param"implicit cs: ContextShift[F]",
              Http4sMeta.endpoints(tapiroRoutes, authTokenName),
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
    authTokenName: String,
    httpEndpointsName: String,
    requiredPackages: Set[Import],
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
              requiredPackages,
              Type.Name(controllerName),
              Term.Name(tapirEndpointsName),
              Type.Name(authTokenName),
              Term.Name(httpEndpointsName),
              Meta.codecsImplicits(tapiroRoutes, authTokenName),
              AkkaHttpMeta.endpoints(tapiroRoutes, authTokenName),
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
