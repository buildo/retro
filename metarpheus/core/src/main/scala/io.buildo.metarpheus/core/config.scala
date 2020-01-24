package io.buildo.metarpheus
package core

case class Config(
  modelsForciblyInUse: Set[String] = Set.empty,
  discardRouteErrorModels: Boolean = false,
)

object Config {
  val default: Config = Config()
}
