package testo

import scala.annotation.StaticAnnotation
class query extends StaticAnnotation
class command extends StaticAnnotation

case class CustomError(message: String)

trait Controller[F[_]] {
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
