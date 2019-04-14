import io.buildo.metarpheus.core.{Metarpheus, Config}
import io.buildo.metarpheus.core.intermediate.{API, RouteSegment, Type => MetarpheusType}
import java.io.PrintWriter
import scopt.OParser
import scala.meta._

case class CliConfig(
  from: String = "./",
  to: String = "./Endpoints.scala",
  deps: Option[String] = None
)

object Boot {
  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CliConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("tapiro"),
        head("tapiro", "0.0.1"),
        opt[String]('f', "from")
          .action((x, c) => c.copy(from = x))
          .text("from is a string property"),
        opt[String]('t', "to")
          .action((x, c) => c.copy(to = x))
          .text("to is a string property"),
        opt[Option[String]]('d', "deps")
          .action((x, c) => c.copy(deps = x))
          .text("deps is an option string property"),
      )
    }

    OParser.parse(parser, args, CliConfig()) match {
      case Some(c) =>
        val deps = c.deps.map(d => q"import ${meta.Term.Name(d)}._")
        Util.createFile(c.from, c.to, deps)
      case _ =>
        println("Couldn't read the configurations")
    }
  }
}

object Util {
  import Formatter.format
  import RoutesWrapper.wrapRoutes
  import EndpointConverter.routeToEndpoint

  def createFile(from: String, to: String, deps: Option[meta.Import]) = {
    val metarpheusResult: API = Metarpheus.run(List(from), Config(Set.empty))
    val routes = metarpheusResult.routes
    val name = if (routes.isEmpty) "Endpoints"
      else s"${routes.head.name.head.capitalize}Endpoints"

    val content = format(wrapRoutes(name, deps, routes.map(routeToEndpoint)))
    try {
      val writer = new PrintWriter(to)
      writer.write(content)
      writer.close()
      println(s"generated file $to 🤖")
    } catch {
      case e: Exception => println(e.getLocalizedMessage())
    }
  }
}

object RoutesWrapper {
  val wrapRoutes = (name: String, models: Option[meta.Import], objects: List[meta.Defn.Val]) =>
    models match {
      case Some(models) =>q"""object ${Term.Name(name)} {
  import tapir._
  $models

 ..$objects }
"""
      case None => q"""object ${Term.Name(name)} {
  import tapir._

 ..$objects }
"""
    }
}
