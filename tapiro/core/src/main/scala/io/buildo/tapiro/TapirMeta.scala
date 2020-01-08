package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{
  Route,
  RouteParam,
  TaggedUnion,
  Type => MetarpheusType,
}

import scala.meta._

object TapirMeta {
  import Meta._

  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    name: Term.Name,
    implicits: List[Term.Param],
    body: List[Defn.Val],
  ) =>
    q"""
    package ${`package`} {
      ..${imports.toList.map(i => q"import $i._")}
      import tapir._
      import tapir.Codec.{ JsonCodec, PlainCodec }

      trait ${Type.Name(name.value)} {
        ..${body.map(defn => Decl.Val(defn.mods, defn.pats, defn.decltpe.get))}
      }

      object $name {
        def create(statusCodes: String => Int = _ => 422)(..$implicits) = new ${Init(
      Type.Name(name.value),
      Name.Anonymous(),
      Nil,
    )} { ..${body.map(
      d => d.copy(mods = mod"override" :: d.mods),
    )} }
      }
    }
    """

  val routeToTapirEndpoint = (route: TapiroRoute) =>
    q"val ${Pat.Var(Term.Name(route.route.name.tail.mkString))}: ${endpointType(route.route)} = ${endpointImpl(route)}"

  private[this] val endpointType = (route: Route) => {
    val returnType = toScalametaType(route.returns)
    val argsList = route.params.map(p => toScalametaType(p.tpe)) ++
      route.body.map(b => toScalametaType(b.tpe))
    val argsType = argsList match {
      case Nil         => Type.Name("Unit")
      case head :: Nil => head
      case l           => Type.Tuple(l)
    }
    val error = route.error.map(toScalametaType).getOrElse(Type.Name("String"))
    t"Endpoint[$argsType, $error, $returnType, Nothing]"
  }

  private[this] val endpointImpl = (route: TapiroRoute) => {
    val basicEndpoint = Term.Apply(
      Term
        .Select(Term.Select(Term.Name("endpoint"), Term.Name(route.route.method)), Term.Name("in")),
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
        route.route.error.map(typeNameString).get,
      ),
      route.route.returns,
    )
  }

  private[this] val withBody = (endpoint: meta.Term, tpe: MetarpheusType) => {
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(Term.ApplyType(Term.Name("jsonBody"), List(toScalametaType(tpe)))),
    ),
  }

  private[this] val withError =
    (endpoints: meta.Term, errorValues: List[TaggedUnion.Member], errorName: String) =>
      Term.Apply(
        Term.Select(endpoints, Term.Name("errorOut")),
        List(
          if (errorValues.isEmpty && errorName == "String") Term.Name("stringBody")
          else if (errorValues.isEmpty && errorName != "String") Term.ApplyType(Term.Name("jsonBody"), List(Type.Name(errorName)))
          else listErrors(errorValues, errorName)
        ),
      )

  private[this] val listErrors = (errorValues: List[TaggedUnion.Member], errorName: String) =>
    Term.Apply(
      Term.ApplyType(Term.Name("oneOf"), List(Type.Name(errorName))),
      errorValues.map { error =>
        Term.Apply(
          Term.Name("statusMapping"),
          List(
            Term.Apply(Term.Name("statusCodes"), List(Lit.String(error.name))),
            Term.ApplyType(Term.Name("jsonBody"), List(Type.Name(error.name))),
          ),
        )
      },
    )

  private[this] val withOutput = (endpoint: meta.Term, returnType: MetarpheusType) =>
    Term.Apply(
      Term.Select(endpoint, Term.Name("out")),
      List(
        Term.ApplyType(
          Term.Name("jsonBody"),
          List(toScalametaType(returnType)),
        ),
      ),
    )

  private[this] val withParam = (endpoint: meta.Term, param: RouteParam) => {
    val noDesc =
      Term.Apply(
        Term.Select(endpoint, Term.Name("in")),
        List(
          Term.Apply(
            Term.ApplyType(Term.Name("query"), List(toScalametaType(param.tpe))),
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
