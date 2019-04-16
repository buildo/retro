# metarpheus-tapir
Utility to produce [tapir](https://github.com/softwaremill/tapir) endpoints from [metarpheus](https://github.com/buildo/metarpheus)

## How to build

Run `sbt assembly`

## How to use

`java -jar tapiro.jar -f {input metarpheus path} -t {endpoint file path} -p {module_package}`

## Example

Given:
```
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

```
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

```
val endpoints = new ControllerEndpoints()
val controller = new ControllerImpl[IO]()

val pusho: HttpRoutes[IO] = endpoints.pusho.toRoutes(controller.pusho)
val ghetto: HttpRoutes[IO] = endpoints.ghetto.toRoutes((controller.ghetto _).tupled)

val service: HttpApp[IO] = cats.data.NonEmptyList(pusho, List(ghetto)).reduceK.orNotFound
```