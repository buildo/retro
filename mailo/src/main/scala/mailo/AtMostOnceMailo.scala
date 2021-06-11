package mailo

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import mailo.data.MailData
import mailo.http.MailClient
import scala.concurrent.ExecutionContext

class AtMostOnceMailo(
  val mailData: MailData,
  val mailClient: MailClient,
)(implicit
  ec: ExecutionContext,
  conf: Config = ConfigFactory.load(),
) extends Mailo
    with LazyLogging {
  val emailSender = new EmailSender(mailData, mailClient)
  def send(mail: Mail) = emailSender.send(mail)
  def sendBatch(batch: BatchMail) = emailSender.sendBatch(batch)
}
