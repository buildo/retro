#Mailo

[![Bintray](https://img.shields.io/bintray/v/buildo/maven/mailo.svg)](https://bintray.com/buildo/maven/mailo/view)
[![Build Status](https://drone.our.buildo.io/api/badges/buildo/mailo/status.svg)](https://drone.our.buildo.io/buildo/mailo)

- Styling HTML email is painful.
- Managing templates stored in ESPs is painful.
- Debugging why your email wasn't delivered is painful.

# Mailo Features
- Store your email templates where you like.
- Send your email with the ESP you like.
- Compose your template using HTML partials (convenient for footers and headers).
- Collect your email errors in "just one point".

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
