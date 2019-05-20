package mailo.data

import mailo.MailError
import mailo.MailRawContent

import scala.concurrent.Future

trait MailData {
  def get(name: String): Future[Either[MailError, MailRawContent]]
}
