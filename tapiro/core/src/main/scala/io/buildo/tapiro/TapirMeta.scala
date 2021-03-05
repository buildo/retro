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
      ..${imports.toList.sortWith(_.toString < _.toString).map(i => q"import $i._")}
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

        ..${postInputClassDeclarations}
      }
    }
    """

  val routeToTapirEndpoint = (tapirEndpointsName: Term.Name) =>
    (route: TapiroRoute) =>
      q"val ${Pat.Var(Term.Name(route.route.name.tail.mkString))}: ${endpointType(tapirEndpointsName, route)} = ${endpointImpl(route)}"

  private[this] val endpointType = (tapirEndpointsName: Term.Name, route: TapiroRoute) => {
    val returnType = metarpheusTypeToScalametaType(route.route.returns)
    val argsType = route.method match {
      case RouteMethod.GET =>
        val argsList = route.route.params.map(routeParamToScalametaType)
        argsList match {
          case Nil         => Type.Name("Unit")
          case head :: Nil => head
          case l           => Type.Tuple(l)
        }
      case RouteMethod.POST =>
        val authTokenType = route.route.params
          .filter(_.tpe == MetarpheusType.Name(authTokenName))
          .map(t => metarpheusTypeToScalametaType(t.tpe))
          .headOption
        val inputType = Type.Select(tapirEndpointsName, postInputType(route.route))
        authTokenType match {
          case Some(t) => Type.Tuple(List(inputType, t))
          case None    => inputType
        }
    }
    val error = metarpheusTypeToScalametaType(route.error match {
      case RouteError.TaggedUnionError(t) => MetarpheusType.Name(t.name)
      case RouteError.OtherError(t)       => t
    })
    t"Endpoint[$argsType, $error, $returnType, Nothing]"
  }

  private[this] val endpointImpl = (route: TapiroRoute) => {
    val method = route.method match {
      case RouteMethod.GET  => "get"
      case RouteMethod.POST => "post"
    }
    val basicEndpoint = Term.Apply(
      Term
        .Select(Term.Select(Term.Name("endpoint"), Term.Name(method)), Term.Name("in")),
      List(Lit.String(route.route.name.tail.mkString)),
    )
    withOutput(
      withError(
        withParams(basicEndpoint, route),
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

  private[this] val withParams = (endpoint: meta.Term, route: TapiroRoute) => {
    val (auth, params) = route.route.params.partition(_.tpe == MetarpheusType.Name(authTokenName))
    val endpointWithParams = route.method match {
      case RouteMethod.GET =>
        params.foldLeft(endpoint) { (acc, param) =>
          withParam(acc, param)
        }
      case RouteMethod.POST =>
        withBody(endpoint, route.route)
    }
    auth match {
      case Nil => endpointWithParams
      case _   => withAuth(endpointWithParams)
    }
  }

  private[this] val withBody = (endpoint: meta.Term, route: Route) => {
    Term.Apply(
      Term.Select(endpoint, Term.Name("in")),
      List(Term.ApplyType(Term.Name("jsonBody"), List(postInputType(route)))),
    ),
  }

  private[this] val withError =
    (endpoints: meta.Term, routeError: RouteError) =>
      routeError match {
        case RouteError.OtherError(t) if typeNameString(t) == "Unit" => endpoints
        case _ =>
          Term.Apply(
            Term.Select(endpoints, Term.Name("errorOut")),
            List(
              routeError match {
                case RouteError.TaggedUnionError(taggedUnion) =>
                  listErrors(taggedUnion)
                case RouteError.OtherError(MetarpheusType.Name("String")) =>
                  Term.Name("stringBody")
                case RouteError.OtherError(t) =>
                  Term.ApplyType(Term.Name("jsonBody"), List(metarpheusTypeToScalametaType(t)))
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
              List(metarpheusTypeToScalametaType(returnType)),
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
            Term.ApplyType(Term.Name("query"), List(routeParamToScalametaType(param))),
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
    route.method match {
      case RouteMethod.POST =>
        val params = route.route.params
          .filterNot(_.tpe == MetarpheusType.Name(authTokenName))
          .map { p =>
            param"${Term.Name(p.name.getOrElse(typeNameString(p.tpe)))}: ${metarpheusTypeToScalametaType(p.tpe)}"
          }
        List(q"case class ${postInputType(route.route)}(..$params)")
      case RouteMethod.GET =>
        Nil
    }

  val routeCodecDeclarations = (route: TapiroRoute) => {
    val mkDeclaration = (s: String) => {
      val name = Pat.Var(Term.Name(route.route.name.tail.mkString + "RequestPayload" + s))
      val tpe = postInputType(route.route)
      val fun = Term.Name("derive" + s)
      q"implicit val $name : ${Type.Name(s)}[$tpe] = $fun"
    }
    route.method match {
      case RouteMethod.POST =>
        List("Decoder", "Encoder").map(mkDeclaration)
      case RouteMethod.GET =>
        Nil
    }
  }
}
