package testo

import scala.annotation.StaticAnnotation
class query extends StaticAnnotation
class command extends StaticAnnotation

case class Errore(message: String)

trait Controller[F[_]] {
  @query
  def ghetto(
    i: Int,
    s: String,
  ): F[Either[Errore, SpittyCash]]

  @command
  def pusho(
    spitty: SpittyCash,
  ): F[Either[String, String]]
}
