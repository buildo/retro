package mailo.data

import awscala.s3.{Bucket, S3}
import awscala.Credentials
import com.typesafe.config.ConfigFactory
import com.amazonaws.regions.RegionUtils
import cats.syntax.either._
import cats.syntax.traverse._
import cats.instances.map._
import cats.instances.either._
import alleycats.std.all._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import mailo.MailError

import scala.language.postfixOps

object S3MailDataError {
  case object ObjectNotFound extends MailError("S3 object not found")
  case object BucketNotFound extends MailError("S3 bucket not found")
  case class S3InternalError(message: String) extends MailError(message)
}

class S3MailData(
  implicit
  ec: ExecutionContext,
  conf: com.typesafe.config.Config = ConfigFactory.load()
) extends MailData {
  import mailo.MailRawContent
  import S3MailDataError._
  type EitherMailError[A] = Either[MailError, A]

  private[S3MailData] case class S3Config(
    key: String,
    secret: String,
    bucket: String,
    regionName: String,
    partialsFolder: String
  )

  private[S3MailData] val s3Config = S3Config(
    key = conf.getString(s"mailo.s3.key"),
    secret = conf.getString(s"mailo.s3.secret"),
    bucket = conf.getString(s"mailo.s3.bucket"),
    regionName = conf.getString(s"mailo.s3.region"),
    partialsFolder = conf.getString(s"mailo.s3.partialsFolder")
  )

  private[S3MailData] implicit val region = RegionUtils.getRegion(s3Config.regionName)
  private[S3MailData] implicit val s3 = S3(new Credentials(s3Config.key, s3Config.secret))
  private[S3MailData] def bucket: Option[Bucket] = s3.bucket(s3Config.bucket)

  def get(name: String): Future[EitherMailError[MailRawContent]] = Future {
    val folder = s3Config.partialsFolder
    for {
      template <- getObject(name)
      partials <- getObjects(s3Config.partialsFolder)
      //filtering partial objects dropping initial chars
      partialObjects <- {
        val result: EitherMailError[Map[String, String]] = (partials
          .map(n => n.drop(folder.length + 1) -> getObject(n)).toMap)
          .sequence[EitherMailError, String]
        result
      }
    } yield (MailRawContent(template, partialObjects))
  }

  //http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
  private[this] def convertStreamToString(is: java.io.InputStream): String = {
    val s: java.util.Scanner = new java.util.Scanner(is).useDelimiter("\\A")
    if (s.hasNext()) s.next() else ""
  }

  private[this] def getObjects(folder: String): Either[MailError, Set[String]] = {
    try {
      bucket match {
        case Some(b) => b.keys(folder).toSet.filter(_ != s"$folder/").asRight[MailError]
        case None    => ObjectNotFound.asLeft[Set[String]]
      }
    } catch {
      case e: Exception => S3InternalError(e.getMessage).asLeft[Set[String]]
    }
  }

  private[this] def getObject(name: String): Either[MailError, String] =
    try {
      bucket match {
        case Some(b) =>
          b.getObject(name) match {
            case Some(o) => convertStreamToString(o.content).asRight[MailError]
            case None    => ObjectNotFound.asLeft[String]
          }
        case None => BucketNotFound.asLeft[String]
      }
    } catch {
      case e: Exception => S3InternalError(e.getMessage).asLeft[String]
    }
}
