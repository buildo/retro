package testo

import cats.effect.{IO, ContextShift}
import cats.implicits._
import tapir.json.circe._
import io.circe.generic.auto._

import cats.effect._

import org.http4s._

import org.http4s.server.blaze._
import org.http4s.implicits._

import tapir.server.http4s._

class ControllerImpl[F[_]](implicit F: Sync[F]) extends Controller[F] {
  def ghetto(
    i: Int,
    s: String,
  ): F[Either[Errore, SpittyCash]] =
    if (i == 1) F.delay(Right(SpittyCash("spitty", 1.0)))
    else F.delay(Left(Errore("nope")))

  def pusho(
    spitty: SpittyCash,
  ): F[Either[String, String]] = F.delay(Right("ok"))
}

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = 
  IO.contextShift(scala.concurrent.ExecutionContext.global)

  val endpoints = new ControllerEndpoints()
  val controller = new ControllerImpl[IO]()

  val app = (new ControllerHttp4sEndpoints(controller)).app

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8083, "localhost")
      .withHttpApp(app)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

object OpenApi extends App {
  import tapir.openapi.OpenAPI
  import tapir.docs.openapi._
  import tapir.openapi.circe.yaml._

  val endpoints = new ControllerEndpoints()
  
  val docs: OpenAPI = endpoints.ghetto.toOpenAPI("Ghetto", "1.0")
  println(docs.toYaml)
}

object Cliente extends App {
  // import tapir.client.sttp._
  // import com.softwaremil
  // val endpoints = new ControllerEndpoints()

  // val booksListingRequest: Request[Either[String, , Nothing] = endpoints
  //   .toSttpRequest(uri"http://localhost:8083")
}
