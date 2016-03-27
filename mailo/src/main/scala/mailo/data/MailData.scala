package mailo.data

import mailo.MailError
import scala.concurrent.Future
import scalaz.\/

import mailo.MailRawContent

trait MailData {
  def get(name: String): Future[mailo.MailError \/ MailRawContent]
}
