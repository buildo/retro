import io.buildo.metarpheus.core.intermediate.{Route, RouteSegment, RouteParam, Type => MetarpheusType}
import scala.meta._

object EndpointConverter {
  val typeName = (`type`: MetarpheusType) => Type.Name(typeNameString(`type`))

  val typeNameString = (`type`: MetarpheusType) => `type` match {
    case MetarpheusType.Apply(name, _) => name
    case MetarpheusType.Name(name) => name
  }

  val endpointType = (route: Route) => {
    val returnType = typeName(route.returns)
    val argsList = route.params.map(p => typeName(p.tpe)) ++
      route.body.map(b => typeName(b.tpe))
    val argsType = argsList match {
      case Nil => Type.Name("Unit")
      case head :: Nil => head
      case l => Type.Tuple(l)
    }
    t"Endpoint[$argsType, String, $returnType, Nothing]"
  }

  val endpointImpl = (route: Route) => {
    val basicEndpoint = Term.Apply(Term.Select(Term.Select(Term.Name("endpoint"),
      Term.Name(route.method)),
       Term.Name("in")),
       List(Lit.String(route.name.tail.mkString)))
    withOutput(
      withError(
        withBody(
          route.params.foldLeft(basicEndpoint){(acc, param) => withParam(acc, param)}, route.body
        )
      ), route.returns
    )
  }

  val withBody = (endpoint: meta.Term, body: Option[Route.Body]) => {
    body match {
      case None => endpoint
      case Some(body) => Term.Apply(Term.Select(endpoint,
        Term.Name("in")), List(Term.ApplyType(Term.Name("jsonBody"),
          List(Type.Name(typeNameString(body.tpe))))))
    }
  }

  val withError = (endpoints: meta.Term) =>
    Term.Apply(Term.Select(endpoints, Term.Name("errorOut")), List(Term.Name("stringBody")))

  val withOutput = (endpoint: meta.Term, returnType: MetarpheusType) =>
    Term.Apply(
      Term.Select(endpoint, Term.Name("out")),
      List(Term.ApplyType(
        Term.Name("jsonBody"),
        List(typeName(returnType))
      ))
    )

  val withParam = (endpoint: meta.Term, param: RouteParam) => {
    val noDesc =
        Term.Apply(
          Term.Select(endpoint,
            Term.Name("in")),
            List(
              Term.Apply(
                Term.ApplyType(Term.Name("query"), List(Type.Name(typeNameString(param.tpe)))),
                List(Lit.String(param.name.getOrElse(typeNameString(param.tpe))))
              )
            ))

    param.desc match {
      case None => noDesc
      case Some(desc) => Term.Apply(
        Term.Select(noDesc,
          Term.Name("description")),
            List(Lit.String(desc)))
    }
  }

  val routeToEndpoint: Route => meta.Defn.Val = route =>
    q"val ${Pat.Var(Term.Name(route.name.tail.mkString))}: ${endpointType(route)} = ${endpointImpl(route)}"
}