package testo

import cats.effect._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import endpoints.ExampleControllerHttpEndpoints
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._

object Boot extends IOApp {
  val exampleController = ExampleController.create[IO]
  val routes = ExampleControllerHttpEndpoints.routes(exampleController)

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
