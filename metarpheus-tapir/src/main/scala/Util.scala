import io.buildo.metarpheus.core.{Metarpheus, Config}
import io.buildo.metarpheus.core.intermediate.{API, RouteSegment, Type => MetarpheusType, Route}
import java.io.PrintWriter
import scala.meta._

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