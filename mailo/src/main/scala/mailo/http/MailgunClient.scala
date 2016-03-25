package mailo.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.actor.ActorSystem

import scala.concurrent.Future

import com.typesafe.config.ConfigFactory

import scalaz.\/
import scalaz.syntax.either._

class MailgunClient(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) extends MailClient {
  import mailo.MailoError
  import MailClientError._
  import mailo.MailRefinedContent._
  import mailo.MailResponse

  private[this] case class MailgunConfig(key: String, uri: String)
  private[this] val conf = ConfigFactory.load()
  private[this] val mailgunConfig = MailgunConfig(
    key = conf.getString("mailgun.key"),
    uri = conf.getString("mailgun.uri")
  )

  def send(
    to: String,
    from: String,
    subject: String,
    content: MailRefinedContent,
    tags: List[String]
  )(implicit
    executionContext: scala.concurrent.ExecutionContext
  ): Future[MailoError \/ MailResponse] = {
    import de.heikoseeberger.akkahttpcirce.CirceSupport._
    import io.circe.generic.auto._

    val auth = Authorization(BasicHttpCredentials("api", mailgunConfig.key))

    for {
      entity <- entity(from = from, to = to, subject = subject, content = content, tags = tags)
      request = HttpRequest(
        method = HttpMethods.POST,
        uri = s"${mailgunConfig.uri}/messages",
        headers = List(auth),
        entity = entity
      )
      response <- Http().singleRequest(request)
      result <- response.status.intValue() match {
        case 200 => Unmarshal(response.entity).to[MailResponse].map(_.right[MailoError])
        case 400 => Future(BadRequest.left[MailResponse])
        case 401 => Future(Unauthorized.left[MailResponse])
        case 402 => Future(RequestFailed.left[MailResponse])
        case 404 => Future(NotFound.left[MailResponse])
        case 500 | 502 | 503 | 504 => Future(ServerError.left[MailResponse])
        case _ => Future(UnknownCode.left[MailResponse])
      }
    } yield result
  }

  private[this] def entity(
    from: String,
    to: String,
    subject: String,
    content: MailRefinedContent,
    tags: List[String]
  )(implicit
    executionCon: scala.concurrent.ExecutionContext
  ): Future[RequestEntity] = {
    import mailo.MailRefinedContent._
    val tagsForm = tags map (Multipart.FormData.BodyPart.Strict("o:tag", _))

    val contentForm = content match {
      case HTMLContent(html) => Multipart.FormData.BodyPart.Strict("html", html)
      case TEXTContent(text) => Multipart.FormData.BodyPart.Strict("text", text)
    }

    val multipartForm = Multipart.FormData(Source(List(
      Multipart.FormData.BodyPart.Strict("from", from),
      Multipart.FormData.BodyPart.Strict("to", to),
      Multipart.FormData.BodyPart.Strict("subject", subject)
    ) ++ tagsForm :+ contentForm ))

    Marshal(multipartForm).to[RequestEntity]
  }
}
