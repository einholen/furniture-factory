package net.nastich.factory.actor

import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
import spray.json.DefaultJsonProtocol._
import spray.json._

/**
 * @author sena0713
 * @since 19.09.2014 
 */
class Manufacturer extends Actor with ActorLogging {

  import net.nastich.factory.actor.Manufacturer._

  def receive = LoggingReceive {
    case Shop(Chair) =>
      log.info("Ordered chair")
      sender() ! OrderComplete(Chair, 100.50)
    case Shop(Table) =>
      log.info("Ordered table")
      sender() ! OrderComplete(Chair, 200.60)
  }

}

object Manufacturer {

  def props = Props(new Manufacturer)

  case class Shop(item: Item)

  sealed trait Item {
    val text: String
  }

  object Item {
    def apply(text: String): Option[Item] = text match {
      case Chair.text => Some(Chair)
      case Table.text => Some(Table)
      case _ => None
    }
    def unapply(text: String): Option[Item] = apply(text)
  }

  case object Chair extends Item {
    val text = "chair"
  }
  
  case object Table extends Item {
    val text = "table"
  }

  /** This message is expected to be received in response to  */
  case class OrderComplete(item: Item, billAmount: BigDecimal)

}