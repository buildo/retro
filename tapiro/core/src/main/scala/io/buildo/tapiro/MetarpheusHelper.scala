package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType, Model, TaggedUnion, Route}

object MetarpheusHelper {
  def routeError(route: Route, models: List[Model]): TapiroRouteError =
    route.error.map { error =>
      val errorName = error match {
        case MetarpheusType.Name(name)     => name
        case MetarpheusType.Apply(name, _) => name
      }

      val candidates = models.collect {
        case tu @ TaggedUnion(name, _, _, _) if name == errorName => tu
      }
      if (candidates.length > 1) throw new Exception(s"ambiguous error type name $errorName")
      else
        candidates.headOption
          .map(TapiroRouteError.TaggedUnionError.apply)
          .getOrElse(TapiroRouteError.OtherError(error))
    }.getOrElse(TapiroRouteError.OtherError(MetarpheusType.Name("String")))
}
