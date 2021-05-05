---
id: introduction
title: Introduction
---

`mailo` is a library that allows you to:

- Store your email templates where you like.
- Send your email with the ESP you like.
- Compose your template using HTML partials (convenient for footers and
  headers).
  - Allowed chars in the names of the partials are given by the following regex
    `a-zA-Z0-9_.-`
- Conveniently collect errors.

# Getting started

First, you need to obtain a instance of `Mailo`, here's an example:

```scala mdoc:invisible
import com.typesafe.config.ConfigFactory

implicit val dummyConfig = ConfigFactory.parseString("""
  mailo {
    s3 {
      key = "some-key"
      secret = "some-secret"
      bucket = "some-bucket"
      region = "eu-central-1"
      partialsFolder = "partials"
    }
    mailgun {
      key = "some-key"
      uri = "some-uri"
    }
    cachingTTLSeconds = 30
  }
""")
```

```scala mdoc
import akka.actor.ActorSystem

import mailo.Mailo
import mailo.data.S3MailData
import mailo.http.MailgunClient

import scala.concurrent.ExecutionContext.Implicits.global


implicit val system = ActorSystem()

val s3 = new S3MailData()
val mailgun = new MailgunClient()

val mailer = Mailo(s3, mailgun)
```

Then you can send an email like so:

```scala mdoc
import mailo.Mail

mailer.send(
  Mail(
    to = "recipient@mail.com",
    from = "Mailo sender@mail.com",
    templateName = "mail.html",
    subject = "Test mail",
    params = Map("hi" -> "Hello this is your first email! :D"),
    tags = List("test")
  )
)
```

## Templates

Use double curly breakets for parameters `{{parameter}}`, remember to include
parameters in `params` argument.

Use double square breakets for partials `[[partial.html]]`. Mailo looks for
partials in `partials` folder.

Complete example:

```html
[[header.html]] Hello {{personFirstName}} {{personLastName}} [[footer.html]]
```

## How to send attachments

Mailo provide a case class `Attachment` that is used to send attachments in
emails. `Attachment` case class is defined as:

```scala
import akka.http.scaladsl.model.ContentType
case class Attachment(
  name: String,
  `type`: ContentType,
  content: String
)
```

An attachment can be created as follows:

```scala mdoc
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._
import mailo.Attachment

val attachment = Attachment(name = "test.txt", content="test", `type`=`text/plain` withCharset `UTF-8`)
```

And sent as:

```scala mdoc
mailer.send(
  Mail(
    to = "recipient@mail.com",
    from = "Mailo sender@mail.com",
    cc = Some("ccrecipient@mail.com"),
    bcc = Some("bccrecipient@mail.com"),
    subject = "Test mail",
    templateName = "mail.html",
    params = Map("ciao" -> "CIAOOOONE"),
    attachments = List(attachment),
    tags = List("test")
  )
)
```

## Content-Transfer-Encoding

You can specify add a Content-Transfer-Encoding header in the attachments as
follows.

```scala mdoc
val attachment2 = Attachment(
  name = "test.pdf",
  content="<<base64pdf>>",
  `type`=`application/pdf`,
  transferEncoding = Some("base64")
)
```

## Templates caching

Since version `0.1.5` templates are cached. You can set caching TTL (time to
live) in the `application.conf` as `mailo.cachingTTLSeconds: 30`.
