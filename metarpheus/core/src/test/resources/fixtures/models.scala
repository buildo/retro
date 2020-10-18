package io.buildo.baseexample

package models

case class CampingName(s: String) extends AnyVal

/** Represents a camping site
  * @param name
  * @param size number of tents
  * @param location camping location
  * @param rating camping rating
  */
case class Camping[A](
  id: java.util.UUID,
  name: CampingName,
  size: Int,
  location: CampingLocation,
  rating: CampingRating,
  a: A,
)

/**  Represents a swan
  *  @param color color of the swan
  */
case class Swan(color: String)

/*
 * Location of the camping site
 */
@enum trait CampingLocation {
  /* Near the sea */
  object Seaside
  /* High up */
  object Mountains
}

/*
 * Rating of the camping site
 */
@indexedEnum trait CampingRating {
  type Index = Int
  /* High */
  object High { 3 }
  /* Medium */
  object Medium { 2 }
  /* Low */
  object Low { 1 }
}

/*
 * Surface of the camping site
 */
sealed trait Surface
object Surface {
  /* Sandy */
  case object Sand extends Surface
  /* Dirt */
  case object Earth extends Surface
}

/*
 * Planet of the camping site
 */
@enum trait Planet {
  /* Earth is a blue planet */
  Earth
  /* Not sure campings exist */
  Another
}

/*
 * Errors that can happen when creating a camping
 */
sealed trait CreateCampingError
object CreateCampingError {

  /**  The name is already in use
    *  @param names suggestions for names that are not in use
    */
  case class DuplicateName(names: SuggestedNames) extends CreateCampingError

  /*
   *  The chosen size is not allowed
   */
  case class SizeOutOfBounds(min: Int, max: Int) extends CreateCampingError
  case object OtherError extends CreateCampingError
}

case class SuggestedNames(names: List[String])

case class IgnoreMe(
  ignore: String,
)
