import io.buildo.metarpheus.core.{Config, Metarpheus}
import io.buildo.metarpheus.core.intermediate.{API, RouteSegment, Type => MetarpheusType, Route}
import java.io.PrintWriter
import scala.meta._

object Util {
  import Formatter.format
  import Meta.{codecsImplicits, endpointsClass, routeToTapirEndpoint}

  def createEndpointsFile(from: String, to: String, `package`: String) = {
    val routes = Metarpheus.run(List(from), Config(Set.empty)).routes
    val className = Type.Name(
      if (routes.isEmpty) "Endpoints"
      else s"${routes.head.name.head.capitalize}Endpoints",
    )
    val packageName = Term.Name(`package`)
    val endpoints = format(
      endpointsClass(
        packageName,
        className,
        codecsImplicits(routes),
        routes.map(routeToTapirEndpoint),
      ),
    )
    try {
      val writer = new PrintWriter(to)
      writer.write(endpoints)
      writer.close()
      println(s"generated file $to 🤖")
    } catch {
      case e: Exception => println(e.getLocalizedMessage())
    }
  }
}
