package mailo.finder

import mailo.MailoError
import scala.concurrent.Future
import scalaz.\/

import mailo.MailRawContent

trait MailContentFinder {
  def find(contentName: String): Future[mailo.MailoError \/ MailRawContent]
}
