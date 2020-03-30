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

  private[this] val authTokenName: String = "AuthToken"

  val `class` = (
    `package`: Term.Ref,
    imports: Set[Term.Ref],
    tapirEndpointsName: Term.Name,
    implicits: List[Term.Param],
    body: List[Defn.Val],
    postInputClassDeclarations: List[Defn.Class],
    postInputCodecDeclarations: List[Defn.Val],
  ) =>
    q"""
    package ${`package`} {
      ..${imports.toList.map(i => q"import $i._")}
      import io.circe.{ Decoder, Encoder }
      import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
      import sttp.tapir._
      import sttp.tapir.json.circe._
      import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
      import sttp.model.StatusCode

      trait ${Type.Name(tapirEndpointsName.value)}[AuthToken] {
        ..${body.map(defn => Decl.Val(defn.mods, defn.pats, defn.decltpe.get))}
      }

      object $tapirEndpointsName {
        def create[AuthToken](statusCodes: String => StatusCode)(..$implicits) = new ${Init(
      Type.Name(tapirEndpointsName.value),
      Name.Anonymous(),
      Nil,
    )}[AuthToken] {
          ..${postInputCodecDeclarations}
          ..${body.map(d => d.copy(mods = mod"override" :: d.mods))}
        }
      }

      ..${postInputClassDeclarations}
    }
    """

  val routeToTapirEndpoint = (route: TapiroRoute) =>
    q"val ${Pat.Var(Term.Name(route.route.name.tail.mkString))}: ${endpointType(route)} = ${endpointImpl(route)}"

  private[this] val endpointType = (route: TapiroRoute) => {
    val returnType = toScalametaType(route.route.returns)
    val argsType = route.route.method match {
      case "get" =>
        val argsList = route.route.params.map(p => toScalametaType(p.tpe))
        argsList match {
          case Nil         => Type.Name("Unit")
          case head :: Nil => head
          case l           => Type.Tuple(l)
        }
      case "post" =>
        postInputType(route.route)
      case _ =>
        throw new Exception("method not supported")
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
    val (auth, _) = route.route.params.partition(_.tpe == MetarpheusType.Name(authTokenName))
    val endpointsWithParams = withParams(basicEndpoint, route.route)
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

  private[this] val withParams = (endpoint: meta.Term, route: Route) => {
    route.method match {
      case "get" =>
        route.params.foldLeft(endpoint) { (acc, param) =>
          withParam(acc, param)
        }
      case "post" =>
        withBody(endpoint, route)
      case _ => throw new Exception("method not supported")
    },
  }

  private[this] val withBody = (endpoint: meta.Term, route: Route) => {
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(Term.ApplyType(Term.Name("jsonBody"), List(postInputType(route)))),
    ),
  }

  private[this] val withError =
    (endpoints: meta.Term, routeError: TapiroRouteError) =>
      routeError match {
        case TapiroRouteError.OtherError(t) if typeNameString(t) == "Unit" => endpoints
        case _ =>
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
      }

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
    typeNameString(returnType) match {
      case "Unit" =>
        endpoint
      case _ =>
        Term.Apply(
          Term.Select(endpoint, Term.Name("out")),
          List(
            Term.ApplyType(
              Term.Name("jsonBody"),
              List(toScalametaType(returnType)),
            ),
          ),
        )
    }

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

  private[this] val postInputType = (route: Route) =>
    Type.Name(route.name.tail.mkString.capitalize + "RequestPayload")

  val routeClassDeclarations = (route: TapiroRoute) =>
    if (route.route.method == "post") {
      val params = route.route.params
        .filterNot(_.tpe == MetarpheusType.Name(authTokenName))
        .map { p =>
          param"${Term.Name(p.name.getOrElse(typeNameString(p.tpe)))}: ${toScalametaType(p.tpe)}"
        }
      List(q"case class ${postInputType(route.route)}(..$params)")
    } else Nil

  val routeCodecDeclarations: TapiroRoute => List[Defn.Val] = (route: TapiroRoute) => {
    val mkDeclaration = (s: String) => {
      val name = Pat.Var(Term.Name(route.route.name.tail.mkString + "RequestPayload" + s))
      val tpe = postInputType(route.route)
      val fun = Term.Name("derive" + s)
      q"implicit val $name : ${Type.Name(s)}[$tpe] = $fun"
    }
    if (route.route.method == "post") {
      List("Decoder", "Encoder").map(mkDeclaration)
    } else Nil
  }
}
