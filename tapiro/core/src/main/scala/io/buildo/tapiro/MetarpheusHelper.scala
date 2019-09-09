package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType, Model, TaggedUnion, Route}

object MetarpheusHelper {
  def routeErrorValues(route: Route, models: List[Model]): List[TaggedUnion.Member] =
    route.error.map { error =>
      val errorName = error match {
        case MetarpheusType.Name(name)     => name
        case MetarpheusType.Apply(name, _) => name
      }

      models.collect {
        case TaggedUnion(name, values, _, _) if name == errorName => values
      }.flatten
    }.getOrElse(Nil)
}
