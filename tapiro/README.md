# metarpheus-tapir
Utility to produce [tapir](https://github.com/softwaremill/tapir) endpoints from [metarpheus](https://github.com/buildo/metarpheus)

## How to build

Run `sbt assembly`

## How to use

`java -jar tapiro.jar -f FROM -t TO -p PACKAGE`

where

* `FROM` is the absolute or relative path to the Scala sources
* `TO` is the absolute or relative path where tapiro outputs Scala code
* `PACKAGE` is the package name tapiro output code belongs to

## Example

Given:
```scala
package testo

import scala.annotation.StaticAnnotation
class query extends StaticAnnotation
class command extends StaticAnnotation

trait Controller[F[_]] {
  @query
  def ghetto(
    i: Int,
    s: String 
  ): F[Either[String, SpittyCash]]

  @command
  def pusho(
    spitty: SpittyCash,
  ): F[Either[String, String]]
}
```

Generates:

```scala
package testo
import tapir._
import tapir.Codec.JsonCodec

class ControllerEndpoints(
    implicit spittyCash: JsonCodec[SpittyCash],
    string: JsonCodec[String]
) {
  import testo._

  val ghetto: Endpoint[(Int, String), String, SpittyCash, Nothing] =
    endpoint.get
      .in("ghetto")
      .in(query[Int]("i"))
      .in(query[String]("s"))
      .errorOut(stringBody)
      .out(jsonBody[SpittyCash])

  val pusho: Endpoint[SpittyCash, String, String, Nothing] = endpoint.post
    .in("pusho")
    .in(jsonBody[SpittyCash])
    .errorOut(stringBody)
    .out(jsonBody[String])
}
```

Endpoints can be used as follows:

```scala
val endpoints = new ControllerEndpoints()
val controller = new ControllerImpl[IO]()

val pusho: HttpRoutes[IO] = endpoints.pusho.toRoutes(controller.pusho)
val ghetto: HttpRoutes[IO] = endpoints.ghetto.toRoutes((controller.ghetto _).tupled)

val service: HttpApp[IO] = cats.data.NonEmptyList(pusho, List(ghetto)).reduceK.orNotFound
```
