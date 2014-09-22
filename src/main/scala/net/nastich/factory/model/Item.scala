package net.nastich.factory.model

/**
 * This is the root of the hierarchy of Items available for order.
 *
 * @author sena0713
 * @since 22.09.2014 
 */
sealed abstract class Item(val text: String)

object Item {
  def unapply(text: String): Option[Item] = text match {
    case Chair.text => Some(Chair)
    case Table.text => Some(Table)
    case _ => None
  }
}

/** Represents an Item type Chair. May be used in context of ordering an item as well as signalizing that it's ready. */
case object Chair extends Item("chair")

/** Represents an Item type Table. May be used in context of ordering an item as well as signalizing that it's ready. */
case object Table extends Item("table")
