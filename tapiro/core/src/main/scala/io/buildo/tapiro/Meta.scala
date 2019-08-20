package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Route, RouteParam, TaggedUnion, Type => MetarpheusType}

import scala.meta._

object Meta {
  val http4sClass = (
    `package`: Term.Name,
    controllerName: Type.Name,
    endpointsName: Type.Name,
    implicits: List[Term.Param],
    http4sEndpoints: List[Defn.Val],
    app: Defn.Val,
  ) => {
    val tapirEndpoints = q"private[this] val endpoints = new $endpointsName()"
    val httpsEndpointsName = Type.Name(s"${controllerName.syntax}Http4sEndpoints")
    q"""package ${`package`} {
  import cats.effect._
  import cats.implicits._
  import cats.data.NonEmptyList

  import org.http4s._
  import org.http4s.implicits._

  import tapir.server.http4s._
  import tapir.Codec.JsonCodec

  class $httpsEndpointsName(controller: $controllerName[IO])(..$implicits) {
    ..${tapirEndpoints +: http4sEndpoints :+ app}
  }
}
"""
  }

  val httpApp = (head: Route, tail: List[Route]) => {
    val first = Term.Name(head.name.last)
    val rest = tail.map(a => Term.Name(a.name.last))
    q"val app: HttpApp[IO] = NonEmptyList($first, List(..$rest)).reduceK.orNotFound"
  }

  val http4sEndpoints = (routes: List[Route]) =>
    routes.flatMap { route =>
      val name = Term.Name(route.name.last)
      val endpointsName = Term.Select(Term.Name("endpoints"), name)
      val controllersName = Term.Select(Term.Name("controller"), name)
      val controllerContent =
        if (route.method == "get") Some(Term.Select(Term.Eta(controllersName), Term.Name("tupled")))
        else if (route.method == "post") Some(controllersName)
        else None
      controllerContent.map { content =>
        val toRoutes = Term.Apply(Term.Select(endpointsName, Term.Name("toRoutes")), List(content))
        q"private[this] val ${Pat.Var(name)}: HttpRoutes[IO] = $toRoutes"
      }
    }

  val tapirClass = (
    `package`: Term.Name,
    name: Type.Name,
    implicits: List[Term.Param],
    body: List[Defn.Val],
  ) => {
    q"""package ${`package`} {
  import tapir._
  import tapir.Codec.JsonCodec

  class $name(statusCodes: String => Int = _ => 422)(..$implicits) {
   ..$body
  }
}
""",
  }

  val codecsImplicits = (routes: List[TapiroRoute]) => {
    routes.map { case TapiroRoute(route, errorValues) =>
      (List(route.returns) ++ route.body.map(_.tpe)).map(typeToImplicitParam) ++
        errorValues.map(error => stringToImplicitParam(error.name))
    }.flatten.distinct
  }

  val routeToTapirEndpoint = (route: TapiroRoute) =>
    q"val ${Pat.Var(Term.Name(route.route.name.tail.mkString))}: ${endpointType(route.route)} = ${endpointImpl(route)}"

  private[this] val typeToImplicitParam = (tpe: MetarpheusType) =>
    stringToImplicitParam(typeNameString(tpe))

  private[this] val stringToImplicitParam = (name: String) => {
    val paramName = Term.Name(s"${name.head.toLower}${name.tail}")
    val nameType = Type.Name(name)
    param"implicit ${paramName}: JsonCodec[$nameType]"
  }

  private[this] val typeName = (`type`: MetarpheusType) => Type.Name(typeNameString(`type`))

  private[this] val typeNameString = (`type`: MetarpheusType) =>
    `type` match {
      case MetarpheusType.Apply(name, _) => name
      case MetarpheusType.Name(name)     => name
    }

  private[this] val endpointType = (route: Route) => {
    val returnType = typeName(route.returns)
    val argsList = route.params.map(p => typeName(p.tpe)) ++
      route.body.map(b => typeName(b.tpe))
    val argsType = argsList match {
      case Nil         => Type.Name("Unit")
      case head :: Nil => head
      case l           => Type.Tuple(l)
    }
    val error = Type.Name(route.error.map(typeNameString).getOrElse("String"))
    t"Endpoint[$argsType, $error, $returnType, Nothing]"
  }

  private[this] val endpointImpl = (route: TapiroRoute) => {
    val basicEndpoint = Term.Apply(
      Term.Select(Term.Select(Term.Name("endpoint"), Term.Name(route.route.method)), Term.Name("in")),
      List(Lit.String(route.route.name.tail.mkString)),
    )
    withOutput(
      withError(
        route.route.method match {
          case "get" =>
            route.route.params.foldLeft(basicEndpoint) { (acc, param) =>
              withParam(acc, param)
            }
          case "post" =>
            route.route.params.foldLeft(basicEndpoint) { (acc, param) =>
              withBody(acc, param.tpe)
            }
          case _ => throw new Exception("method not supported")
        },
        route.errorValues,
        route.route.error.map(typeNameString).get
      ),
      route.route.returns,
    )
  }

  private[this] val withBody = (endpoint: meta.Term, tpe: MetarpheusType) => {
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(Term.ApplyType(Term.Name("jsonBody"), List(Type.Name(typeNameString(tpe))))),
    ),
  }

  private[this] val withError = (endpoints: meta.Term, errorValues: List[TaggedUnion.Member], errorName: String) =>
    Term.Apply(
      Term.Select(endpoints,
        Term.Name("errorOut")),
      List(
        if(errorValues.isEmpty) Term.Name("stringBody")
        else errors(errorValues, errorName)
      )
    )

  private[this] val errors = (errorValues: List[TaggedUnion.Member], errorName: String) =>
    Term.Apply(
      Term.ApplyType(
        Term.Name("oneOf"),
        List(Type.Name(errorName))),
      errorValues.map { error =>
        Term.Apply(Term.Name("statusMapping"),
          List(
            Term.Apply(Term.Name("statusCodes"), List(Lit.String(error.name))),
            Term.ApplyType(
              Term.Name("jsonBody"),
              List(Type.Name(error.name)))
          )
        )
      }
    )


  private[this] val withOutput = (endpoint: meta.Term, returnType: MetarpheusType) =>
    Term.Apply(
      Term.Select(endpoint, Term.Name("out")),
      List(
        Term.ApplyType(
          Term.Name("jsonBody"),
          List(typeName(returnType)),
        ),
      ),
    )

  private[this] val withParam = (endpoint: meta.Term, param: RouteParam) => {
    val noDesc =
      Term.Apply(
        Term.Select(endpoint, Term.Name("in")),
        List(
          Term.Apply(
            Term.ApplyType(Term.Name("query"), List(Type.Name(typeNameString(param.tpe)))),
            List(Lit.String(param.name.getOrElse(typeNameString(param.tpe)))),
          ),
        ),
      )

    param.desc match {
      case None => noDesc
      case Some(desc) =>
        Term.Apply(Term.Select(noDesc, Term.Name("description")), List(Lit.String(desc)))
    }
  }
}
