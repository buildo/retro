package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{RouteParam, TaggedUnion, Type => MetarpheusType}

import scala.meta._

object TapirMeta {
  import Meta._

  private[this] val authTokenName: String = "AuthToken"

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
      import sttp.tapir._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode

      trait ${Type.Name(name.value)}[AuthToken] {
        ..${body.map(defn => Decl.Val(defn.mods, defn.pats, defn.decltpe.get))}
      }

      object $name {
        def create[AuthToken](statusCodes: String => StatusCode = _ => StatusCode.UnprocessableEntity)(..$implicits) = new ${Init(
      Type.Name(name.value),
      Name.Anonymous(),
      Nil,
    )}[AuthToken] { ..${body.map(
      d => d.copy(mods = mod"override" :: d.mods),
    )} }
      }
    }
    """

  val routeToTapirEndpoint = (route: TapiroRoute) =>
    q"val ${Pat.Var(Term.Name(route.route.name.tail.mkString))}: ${endpointType(route)} = ${endpointImpl(route)}"

  private[this] val endpointType = (route: TapiroRoute) => {
    val returnType = toScalametaType(route.route.returns)
    val argsList = route.route.params.map(p => toScalametaType(p.tpe)) ++
      route.route.body.map(b => toScalametaType(b.tpe))
    val argsType = argsList match {
      case Nil         => Type.Name("Unit")
      case head :: Nil => head
      case l           => Type.Tuple(l)
    }
    val error = toScalametaType(route.error match {
      case TapiroRouteError.TaggedUnionError(t) => MetarpheusType.Name(t.name)
      case TapiroRouteError.OtherError(t)       => t
    })
    t"Endpoint[$argsType, $error, $returnType, Nothing]"
  }

  private[this] val endpointImpl = (route: TapiroRoute) => {
    val basicEndpoint = Term.Apply(
      Term
        .Select(Term.Select(Term.Name("endpoint"), Term.Name(route.route.method)), Term.Name("in")),
      List(Lit.String(route.route.name.tail.mkString)),
    )
    val (auth, params) = route.route.params.partition(_.tpe == MetarpheusType.Name(authTokenName))
    val endpointsWithParams = withParams(basicEndpoint, route.route.method, params)
    withOutput(
      withError(
        auth match {
          case Nil => endpointsWithParams
          case _   => withAuth(endpointsWithParams)
        },
        route.error,
      ),
      route.route.returns,
    )
  }

  private[this] val withAuth = (endpoint: meta.Term) =>
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(
        Term.Apply(
          Term.ApplyType(
            Term.Name("header"),
            List(Type.Name(authTokenName)),
          ),
          List(Lit.String("Authorization")),
        ),
      ),
    )

  private[this] val withParams =
    (endpoint: meta.Term, method: String, params: List[RouteParam]) => {
      method match {
        case "get" =>
          params.foldLeft(endpoint) { (acc, param) =>
            withParam(acc, param)
          }
        case "post" =>
          params.foldLeft(endpoint) { (acc, param) =>
            withBody(acc, param.tpe)
          }
        case _ => throw new Exception("method not supported")
      },
    }

  private[this] val withBody = (endpoint: meta.Term, tpe: MetarpheusType) => {
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(Term.ApplyType(Term.Name("jsonBody"), List(toScalametaType(tpe)))),
    ),
  }

  private[this] val withError =
    (endpoints: meta.Term, routeError: TapiroRouteError) =>
      Term.Apply(
        Term.Select(endpoints, Term.Name("errorOut")),
        List(
          routeError match {
            case TapiroRouteError.TaggedUnionError(taggedUnion) =>
              listErrors(taggedUnion)
            case TapiroRouteError.OtherError(MetarpheusType.Name("String")) =>
              Term.Name("stringBody")
            case TapiroRouteError.OtherError(t) =>
              Term.ApplyType(Term.Name("jsonBody"), List(toScalametaType(t)))
          },
        ),
      )

  private[this] val listErrors = (taggedUnion: TaggedUnion) =>
    Term.Apply(
      Term.ApplyType(Term.Name("oneOf"), List(Type.Name(taggedUnion.name))),
      taggedUnion.values.map { member =>
        Term.Apply(
          Term.Name("statusMapping"),
          List(
            Term.Apply(Term.Name("statusCodes"), List(Lit.String(member.name))),
            Term.ApplyType(Term.Name("jsonBody"), List(taggedUnionMemberType(taggedUnion)(member))),
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
