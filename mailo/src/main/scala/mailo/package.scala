package mailo

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
    deliverySemantic: DeliverySemantic = DeliverySemantic.AtMostOnce
  )(
    implicit
    ec: ExecutionContext,
    conf: Config = ConfigFactory.load()
  ) = deliverySemantic match {
    case DeliverySemantic.AtMostOnce => new AtMostOnceMailo(mailData, mailClient)
    case DeliverySemantic.AtLeastOnce => new AtLeastOnceMailo(mailData, mailClient)
  }
}
