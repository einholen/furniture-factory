package net.nastich.factory.model

import java.util.UUID

/**
 * Base trait for Parts of items. Subclasses represent an actually produced part.
 * Requests for parts, if needed, should be processed using the [[net.nastich.factory.model.Part.PartType]] type.
 */
sealed trait Part {
  val id: String
}

object Part {
  type PartType = Class[_ <: Part]
}

/** Furniture leg. May be a chair leg or a table leg, a Master generally specializes on producing any subclass of it.
  */
sealed trait Leg extends Part

/** Chair top. May be a chair seat or a chair back, a Master generally specializes on producing any subclass of it */
sealed trait ChairTop extends Part

/** Represents a produced table leg that may be used to construct a [[net.nastich.factory.model.Table]] */
case class TableLeg(id: String = UUID.randomUUID().toString) extends Leg

/** Represents a produced table top that may be used to construct a [[net.nastich.factory.model.Table]] */
case class TableTop(id: String = UUID.randomUUID().toString) extends Part

/** Represents a produced chair leg that may be used to construct a [[net.nastich.factory.model.Chair]] */
case class ChairLeg(id: String = UUID.randomUUID().toString) extends Leg

/** Represents a produced chair seat that may be used to construct a [[net.nastich.factory.model.Chair]] */
case class ChairSeat(id: String = UUID.randomUUID().toString) extends ChairTop

/** Represents a produced chair back that may be used to construct a [[net.nastich.factory.model.Chair]] */
case class ChairBack(id: String = UUID.randomUUID().toString) extends ChairTop
