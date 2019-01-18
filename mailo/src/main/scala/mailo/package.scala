package mailo

import akka.actor.{ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import mailo.data.MailData
import mailo.http.MailClient

import scala.concurrent.{ExecutionContext, Future}

trait Mailo {
  def send(mail: Mail): Future[Either[MailError, MailResult]]
}

object Mailo {
  def apply(
    mailData: MailData,
    mailClient: MailClient,
    deliverySemantic: DeliveryGuarantee = DeliveryGuarantee.AtMostOnce
  )(
    implicit
    ec: ExecutionContext,
    conf: Config = ConfigFactory.load(),
    system: ActorSystem = ActorSystem("mailo")
  ) = deliverySemantic match {
    case DeliveryGuarantee.AtMostOnce => new AtMostOnceMailo(mailData, mailClient)
    case DeliveryGuarantee.AtLeastOnce => new AtLeastOnceMailo(mailData, mailClient)
  }
}
