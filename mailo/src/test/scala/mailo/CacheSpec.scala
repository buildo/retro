package mailo

import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.concurrent.{ ScalaFutures, Eventually}

import mailo.data.S3MailData

import scalacache._
import guava._
import scala.concurrent.duration._

//It's a test, this is fine
import scala.concurrent.ExecutionContext.Implicits.global

//NOTE: run tests in this suite serially
class CacheSpec extends FlatSpec with Matchers with ScalaFutures with Eventually{

  implicit val defaultPatience =
      PatienceConfig(timeout = 20.seconds, interval = 5.seconds)

  implicit val scalaCache = ScalaCache(GuavaCache())
  private[this] val s3 = new S3MailData()

  val templateName1 = "mail.html"
  val templateName2 = "mail-image.html"

  def action(templateName: String, ttl: Int) = cachingWithTTL(templateName)(ttl.seconds) {
    s3.get(templateName)
  }

  "cache" should "initially be empty" in {
    get[String, NoSerialization](templateName1).futureValue should be (None)
    get[String, NoSerialization](templateName2).futureValue should be (None)
  }

  "cache" should "be populated correctly " in {
    action(templateName1, 10).futureValue
    action(templateName2, 30).futureValue

    get[String, NoSerialization](templateName2).futureValue should not be (None)
    get[String, NoSerialization](templateName1).futureValue should not be (None)
  }

  "eventually the cache of the first template" should "expire" in {
    eventually (timeout(20.seconds)) {
      get[String, NoSerialization](templateName1).futureValue should be (None)
      get[String, NoSerialization](templateName2).futureValue should not be (None)
    }
  }

  "cache" should "work correctly after" in {
    action(templateName1, 1).futureValue
    action(templateName2, 1).futureValue
  }
}
