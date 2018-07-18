package mailo

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import mailo.data.MailData
import mailo.http.MailClient
import scala.concurrent.duration._

import scala.util.{Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

import scala.language.postfixOps

case class MailPersistenceError(message: String) extends MailError(message)

class AtLeastOnceMailo(
  val mailData: MailData,
  val mailClient: MailClient,
)(
  implicit
  ec: ExecutionContext,
  conf: Config = ConfigFactory.load(),
  system: ActorSystem = ActorSystem("mailo"),
) extends Mailo
    with LazyLogging {
  implicit val timeout = Timeout(5 seconds)

  //return type of actor ask pattern
  def ask: Future[Any] = ???

  def send(mail: Mail) = {
    ask.map {
      case Success(result) => Right(LocallyQueued)
      case Failure(error)  => Left(MailPersistenceError(error.getLocalizedMessage))
    }
  }
}
