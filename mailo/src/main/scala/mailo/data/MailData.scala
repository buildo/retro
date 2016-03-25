package mailo.data

import mailo.MailoError
import scala.concurrent.Future
import scalaz.\/

import mailo.MailRawContent

trait MailData {
  def get(name: String): Future[mailo.MailoError \/ MailRawContent]
}
