package mailo.finder

import awscala.s3.{ S3, Bucket, S3ObjectSummary, S3Object }
import awscala.{ Credentials, Region }
import nozzle.server.ServerConfig

import com.typesafe.config.ConfigFactory
import scalaz.\/
import scalaz.syntax.either._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import mailo.MailoError
import scala.language.postfixOps

object S3MailContentError {
  case object ObjectNotFound extends MailoError("S3 object not found")
  case object BucketNotFound extends MailoError("S3 bucket not found")
  case class InternalError(message: String) extends MailoError(message)
}

class S3MailContentFinder(implicit
    ec: ExecutionContext
  ) extends MailContentFinder {
  import mailo.MailRawContent
  import S3MailContentError._

  case class S3Config(key: String, secret: String, bucket: String, mocksFolder: String)
  lazy val conf = ConfigFactory.load()

  val s3Config = S3Config(
    key    = conf.getString(s"s3.key"),
    secret = conf.getString(s"s3.secret"),
    bucket = conf.getString(s"s3.bucket"),
    mocksFolder = conf.getString(s"s3.mocksFolder")
  )

  implicit val region = Region.Frankfurt
  implicit val s3 = S3(new Credentials(s3Config.key, s3Config.secret))
  def bucket = s3.bucket(s3Config.bucket)

  def find(name: String) = Future {
    import scalaz._; import Scalaz._
    val folder = s3Config.mocksFolder

    for {
      template <- getObject(name)
      mocks <- getObjects(s3Config.mocksFolder)
      mockObjects <- (mocks map (n => n.drop(folder.length + 1) -> getObject(n)) toMap).sequenceU
    } yield (MailRawContent(template, mockObjects))
  }

  //http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
  private[this] def convertStreamToString(is: java.io.InputStream): String = {
    val s: java.util.Scanner = new java.util.Scanner(is).useDelimiter("\\A")
    if (s.hasNext()) s.next() else ""
  }

  def getObjects(folder: String): MailoError \/ Set[String] = {
    try {
      bucket match {
        case Some(b) => b.keys(folder).toSet.filter(_ != s"$folder/").right[MailoError]
        case None    => ObjectNotFound.left[Set[String]]
      }
    } catch {
      case e: Exception => InternalError(e.getMessage).left[Set[String]]
    }
  }

  private[this] def getObject(name: String): MailoError \/ String =
    try {
      bucket match {
        case Some(b) => b.getObject(name) match {
          case Some(o) => convertStreamToString(o.content).right[MailoError]
          case None    => ObjectNotFound.left[String]
        }
        case None    => BucketNotFound.left[String]
      }
    } catch {
      case e: Exception => InternalError(e.getMessage).left[String]
    }
}
