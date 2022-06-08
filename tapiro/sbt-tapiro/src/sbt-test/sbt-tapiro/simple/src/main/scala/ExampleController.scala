package testo

import cats._
import io.circe.generic.JsonCodec

import scala.annotation.StaticAnnotation
class query extends StaticAnnotation
class command extends StaticAnnotation

@JsonCodec case class CustomError(message: String)

trait ExampleController[F[_], T] {
  @query
  def queryExample(
    intParam: Int,
    stringParam: String,
  ): F[Either[CustomError, CustomModel]]

  @command
  def commandExample(
    body: CustomModel,
  ): F[Either[String, String]]
}

object ExampleController {
  def create[F[_]: Applicative] = new ExampleController[F, String] {
    override def commandExample(body: CustomModel): F[Either[String, String]] =
      Applicative[F].pure(Right(body.name))

    override def queryExample(
      intParam: Int,
      stringParam: String,
    ): F[Either[CustomError, CustomModel]] =
      Applicative[F].pure(Right(CustomModel(stringParam, intParam.toDouble)))
  }
}
