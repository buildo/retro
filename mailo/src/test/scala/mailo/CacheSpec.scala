package mailo

import mailo.data.S3MailData

import scalacache._
import guava._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//NOTE: run tests in this suite serially
class CacheSpec extends munit.FunSuite {

  implicit val scalaCache = ScalaCache(GuavaCache())

  val templateName1 = "mail.html"
  val templateName2 = "mail-image.html"

  def action(templateName: String, ttl: Duration) = cachingWithTTL(templateName)(ttl) {
    val s3 = new S3MailData()
    s3.get(templateName)
  }

  test("cache should initially be empty") {
    for {
      v1 <- get[String, NoSerialization](templateName1)
      v2 <- get[String, NoSerialization](templateName2)
    } yield {
      assert(v1.isEmpty)
      assert(v2.isEmpty)
    }
  }

  test("cache should be populated correctly") {
    for {
      _ <- action(templateName1, ttl = 10.seconds)
      _ <- action(templateName2, ttl = 30.seconds)
      v2 <- get[String, NoSerialization](templateName2)
      v1 <- get[String, NoSerialization](templateName1)
    } yield {
      assert(v1.isDefined)
      assert(v2.isDefined)
    }
  }

  test("eventually the cache of the first template should expire") {
    for {
      _ <- Future(Thread.sleep(20.seconds.toMillis))
      v1 <- get[String, NoSerialization](templateName1)
      v2 <- get[String, NoSerialization](templateName2)
    } yield {
      assert(v1.isEmpty)
      assert(v2.isDefined)
    }
  }

  test("cache should work correctly after") {
    for {
      _ <- action(templateName1, 1.second)
      _ <- action(templateName2, 1.second)
    } yield ()
  }
}
