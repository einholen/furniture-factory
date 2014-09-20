package net.nastich.factory.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.RoundRobinPool
import net.nastich.factory.Settings
import net.nastich.factory.util.HasSequence

/**
 * This actor represents the Furniture Factory (it's not named ''Factory'' to disambiguate between its concept and the
 * similarly-called design pattern). The Manufacturer's general lifecycle consists of accepting shopping orders
 * ([[net.nastich.factory.actor.Manufacturer.Shop]]) and replying with instances of
 * [[net.nastich.factory.actor.Manufacturer.OrderComplete]] wrapping the entity type and the cost of its construction.
 *
 * @author sena0713
 * @since 19.09.2014 
 */
class Manufacturer(settings: Settings) extends Actor with ActorLogging with HasSequence {

  import net.nastich.factory.actor.Assembly._
  import net.nastich.factory.actor.Manufacturer._
  import net.nastich.factory.actor.Master._
  import settings._

  val assembly: ActorRef = context.actorOf(Props.empty//Assembly.props(AssemblyPrice, AssemblyDuration)
    .withRouter(RoundRobinPool(AssemblyCapacity)))

  var freeLegWorkers: List[ActorRef] = List.empty
  var freeChairTopWorkers: List[ActorRef] = List.empty
  var freeTableTopWorkers: List[ActorRef] = List.empty

  var customers: Map[Long, ActorRef] = Map.empty
  var partsForOrders: Map[Long, Seq[Part]] = Map.empty

  def receive = LoggingReceive(acceptOrders orElse acceptParts orElse acceptAssembledProducts)

  val acceptOrders: Receive = {
    case Shop(Chair) =>
      log.info("Ordered chair")
      val orderNo = nextSeq()
      customers += orderNo -> sender()
      self ! Assembled(orderNo, AssemblyPrice, Chair)

    case Shop(Table) =>
      log.info("Ordered table")
      val orderNo = nextSeq()
      customers += orderNo -> sender()
      self ! Assembled(orderNo, AssemblyPrice * 2, Table)
  }

  val acceptParts: Receive = {
    case PartComplete(orderNo, part) =>
      val alreadyCollectedParts = partsForOrders.getOrElse(orderNo, Seq.empty[Part])
      partsForOrders += orderNo -> (part +: alreadyCollectedParts)
  }

  val acceptAssembledProducts: Receive = {
    case Assembled(orderNo, price, product) =>
      customers(orderNo) ! OrderComplete(product, price)
      customers -= orderNo
  }

}

object Manufacturer {

  /** @see [[net.nastich.factory.actor.Manufacturer]] */
  def props(settings: Settings) = Props(new Manufacturer(settings))

  /**
   * This message should be sent to the [[net.nastich.factory.actor.Manufacturer]] actor to order an item.
   * To distinguish which kind of product is ordered, this message should wrap an
   * [[net.nastich.factory.actor.Manufacturer.Item]].
   */
  case class Shop(item: Item)

  sealed abstract class Item(val text: String)
  case object Chair extends Item("chair")
  case object Table extends Item("table")

  object Item {
    def unapply(text: String): Option[Item] = text match {
      case Chair.text => Some(Chair)
      case Table.text => Some(Table)
      case _ => None
    }
  }

  sealed trait Part {
    val id: Long
  }
  case class TableLeg(id: Long) extends Part
  case class TableTop(id: Long) extends Part
  case class ChairLeg(id: Long) extends Part
  case class ChairSeat(id: Long) extends Part
  case class ChairBack(id: Long) extends Part

  /**
   * This message is expected to be replied with to Shopping orders ([[net.nastich.factory.actor.Manufacturer.Shop]])
   */
  case class OrderComplete(item: Item, billAmount: BigDecimal)

}