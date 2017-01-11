package wiro.models

case class ClientConfig(
  host: String,
  port: Int
)

case class ServerConfig(
  host: String,
  port: Int
)

case class WiroRequest(
  args: String
)

case class RpcRequest(
  path: Seq[String],
  args: Map[String, io.circe.Json]
)
