---
id: introduction
title: Introduction
---

`enumero` is a library for concise and safe enumerations in Scala.

```scala mdoc
import io.buildo.enumero.annotations.enum

@enum trait Planet {
  object Earth
  object Venus
  object Mercury
}

Planet.caseToString(Planet.Earth)

Planet.caseFromString("Earth")
```

## Motivation

There are a few options for representing enums in Scala.

The built-in `scala.Enumeration` class has several known issues, which hinder
type-safety.

The community tends to shy away from it and prefer an ADT-based encoding such
as:

```scala mdoc
sealed trait Color
object Color {
  case object Red extends Color
  case object Blue extends Color
  case object Green extends Color
}
```

This approach has many benefits, but it has at least two drawbacks:

- it's verbose
- it doesn't provide a way to (de)serialize enum values to (and from) primitive
  values

`enumero` tries to tackle these two problems by allowing you to write:

```scala mdoc:reset
import io.buildo.enumero.annotations.enum

@enum trait Color {
  object Red
  object Blue
  object Green
}
```

`@enum` is a macro annotation that automatically translates the code above to
the standard ADT-based representation.

`@enum` also provides some utility functions, to make working with enums easier:

```scala mdoc
Color.caseToString(Color.Red)

Color.caseFromString("Red")

Color.values

Color.name
```
