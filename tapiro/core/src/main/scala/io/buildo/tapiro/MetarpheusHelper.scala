package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Model, Route, TaggedUnion, Type => MetarpheusType}

object MetarpheusHelper {
  def toTapiroRoute(models: List[Model])(route: Route): TapiroRoute =
    TapiroRoute(
      route = route,
      method = route.method match {
        case "get"  => RouteMethod.GET
        case "post" => RouteMethod.POST
        case _      => throw new Exception("method not supported")
      },
      error = routeError(route, models),
    )

  def routeError(route: Route, models: List[Model]): RouteError =
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
          .map(RouteError.TaggedUnionError.apply)
          .getOrElse(RouteError.OtherError(error))
    }.getOrElse(RouteError.OtherError(MetarpheusType.Name("String")))
}
