#Mailo

[![Bintray](https://img.shields.io/bintray/v/buildo/maven/mailo.svg)](https://bintray.com/buildo/maven/mailo/view)
[![Build Status](https://drone.our.buildo.io/api/badges/buildo/mailo/status.svg)](https://drone.our.buildo.io/buildo/mailo)

- Store your email templates where you like.
- Send your email with the ESP you like.
- Compose your template using HTML partials (convenient for footers and headers).
- Conveniently collect errors.

# Install
```scala
libraryDependencies += "io.buildo" %% "mailo" % "<MAILO_VERSION>"
```

# How to use
How to get mailo instance.
```scala
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem

import mailo.Mailo
import mailo.data.S3MailData
import mailo.http.MailgunClient

import scala.concurrent.ExecutionContext.Implicits.global

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()

val s3 = new S3MailData()
val mailgun = new MailgunClient()

val mailo = new Mailo(s3, mailgun)
```

How to send an email.
```scala
mailo.send(
  to = "recipient@mail.com",
  from = "Mailo sender@mail.com",
  templateName = "mail.html",
  subject = "Test mail",
  params = Map("hi" -> "Hello this is your first email! :D"),
  tags = List("test")
)
```

# Templates

Use double curly breakets for parameters `{{parameter}}`, remember to include parameters in `params` argument.

Use double square breakets for partials `[[partial.html]]`. Mailo looks for partials in `partial` folder.



Complete example:
```html
[[header.html]]
Hello {{personFirstName}} {{personLastName}}
[[footer.html]]
```

# How to send attachments

Mailo provide a case class `Attachment` that is used to send attachments in emails.
`Attachment` case class is defined as:
```scala
import akka.http.scaladsl.model.ContentType
case class Attachment(
  name: String,
  `type`: ContentType,
  content: String
)
```

An attachment can be created as follows:

```scala
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._
val attachment = Attachment(name = "test.txt", content="test", `type`=`text/plain` withCharset `UTF-8`)
```

And sent as:
```scala
mailer.send(
   to = "recipient@mail.com",
   from = "Mailo sender@mail.com",
   subject = "Test mail",
   templateName = "mail.html",
   params = Map("ciao" -> "CIAOOOONE"),
   attachments = List(attachment),
   tags = List("test")
)
```

# Content-Transfer-Encoding
You can specify add a Content-Transfer-Encoding header in the attachments as follows.

```scala
val attachment = Attachment(name = "test.pdf", content="<<base64pdf>>", `type`=`application/pdf`, transferEncoding = Some("base64"))
```

# Templates caching

Since version `0.1.5` templates are cached.
You can set caching TTL (time to live) in the `application.conf` as `mailo.cachingTTLSeconds: 30`.
