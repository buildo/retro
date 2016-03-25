package mailo.data

import awscala.s3.{ S3, Bucket, S3ObjectSummary, S3Object }
import awscala.{ Credentials, Region }

import com.typesafe.config.ConfigFactory

import scalaz.\/
import scalaz.syntax.either._
import scalaz.syntax.traverse._
import scalaz.std.map._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import mailo.MailoError
import scala.language.postfixOps

object S3MailDataError {
  case object ObjectNotFound extends MailoError("S3 object not found")
  case object BucketNotFound extends MailoError("S3 bucket not found")
  case class S3InternalError(message: String) extends MailoError(message)
}

class S3MailData(implicit
    ec: ExecutionContext
  ) extends MailData {
  import mailo.MailRawContent
  import S3MailDataError._

  private[S3MailData] case class S3Config(key: String, secret: String, bucket: String, mocksFolder: String)
  private[S3MailData] lazy val conf = ConfigFactory.load()

  private[S3MailData] val s3Config = S3Config(
    key    = conf.getString(s"s3.key"),
    secret = conf.getString(s"s3.secret"),
    bucket = conf.getString(s"s3.bucket"),
    mocksFolder = conf.getString(s"s3.mocksFolder")
  )

  private[S3MailData] implicit val region = Region.Frankfurt
  private[S3MailData] implicit val s3 = S3(new Credentials(s3Config.key, s3Config.secret))
  private[S3MailData] def bucket = s3.bucket(s3Config.bucket)

  def get(name: String): Future[\/[MailoError, MailRawContent]] = Future {
    val folder = s3Config.mocksFolder

    for {
      template <- getObject(name)
      mocks <- getObjects(s3Config.mocksFolder)
      //filtering mock objects dropping initial chars
      mockObjects <- (mocks map (n => n.drop(folder.length + 1) -> getObject(n)) toMap).sequenceU
    } yield (MailRawContent(template, mockObjects))
  }

  //http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
  private[this] def convertStreamToString(is: java.io.InputStream): String = {
    val s: java.util.Scanner = new java.util.Scanner(is).useDelimiter("\\A")
    if (s.hasNext()) s.next() else ""
  }

  private[this] def getObjects(folder: String): MailoError \/ Set[String] = {
    try {
      bucket match {
        case Some(b) => b.keys(folder).toSet.filter(_ != s"$folder/").right[MailoError]
        case None    => ObjectNotFound.left[Set[String]]
      }
    } catch {
      case e: Exception => S3InternalError(e.getMessage).left[Set[String]]
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
      case e: Exception => S3InternalError(e.getMessage).left[String]
    }
}
