import io.buildo.metarpheus.core.{Metarpheus, Config}
import io.buildo.metarpheus.core.intermediate.{API, RouteSegment, Type => MetarpheusType, Route}
import java.io.PrintWriter
import scopt.OParser
import scala.meta._

case class CliConfig(
  from: String = "/Users/cale/tmp",
  to: String = "./Endpoints.scala",
  `package`: String = "tapiro",
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
        opt[String]('p', "package")
          .action((x, c) => c.copy(`package` = x))
          .text("package is a string property"),
      )
    }

    OParser.parse(parser, args, CliConfig()) match {
      case Some(c) =>
        Util.createFile(c.from, c.to, c.`package`)
      case _ =>
        println("Couldn't read the configurations")
    }
  }
}

object Util {
  import Formatter.format
  import EndpointConverter.{routeToEndpoint, wrapRoutes, implicits}

  def createFile(from: String, to: String, `package`: String) = {
    val metarpheusResult: API = Metarpheus.run(List(from), Config(Set.empty))
    val routes = metarpheusResult.routes
    val name = Type.Name(if (routes.isEmpty) "Endpoints"
      else s"${routes.head.name.head.capitalize}Endpoints")
    val packageTerm = Term.Name(`package`)

    val content = format(wrapRoutes(name, implicits(routes), packageTerm, routes.map(routeToEndpoint)))
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