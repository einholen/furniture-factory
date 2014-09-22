package net.nastich.factory.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.RoundRobinPool
import net.nastich.factory.Settings
import net.nastich.factory.model._
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

  val assembly: ActorRef = context.actorOf(
    Assembly.props(AssemblyPrice, AssemblyDuration).withRouter(RoundRobinPool(AssemblyCapacity)), "assembly")

  var freeLegWorkers: List[ActorRef] = List.empty
  var freeChairTopWorkers: List[ActorRef] = List.empty
  var freeTableTopWorkers: List[ActorRef] = List.empty

  var orders: Map[Long, Item] = Map.empty
  var customers: Map[Long, ActorRef] = Map.empty
  var invoices: Map[Long, BigDecimal] = Map.empty.withDefaultValue(BigDecimal("0.00"))
  
  var collectedParts: Map[Long, Seq[Part]] = Map.empty.withDefaultValue(Seq.empty[Part])

  def receive = acceptOrders orElse acceptParts orElse acceptAssembledProducts

  val acceptOrders: Receive = {
    case Shop(item) =>
      val orderNo = nextSeq()
      log.debug(s"An order #$orderNo for a $item arrived")
      orders += orderNo -> item
      customers += orderNo -> sender()
      item match {
        case Chair =>
          for (i <- 1 to 4) nextLegWorker() ! PartRequest(orderNo, classOf[ChairLeg])
          nextChairWorker() ! PartRequest(orderNo, classOf[ChairSeat])
          nextChairWorker() ! PartRequest(orderNo, classOf[ChairBack])
          
        case Table =>
          for (_ <- 1 to 4) nextLegWorker() ! PartRequest(orderNo, classOf[TableLeg])
          nextTableTopWorker() ! PartRequest(orderNo, classOf[TableTop])
      }
  }

  val acceptParts: Receive = {
    case PartComplete(orderNo, price, part) =>
      part match {
        case _: Leg => freeLegWorkers ::= sender()
        case _: ChairTop => freeChairTopWorkers ::= sender()
        case _: TableTop => freeTableTopWorkers ::= sender()
      }
      val partsForOrder = part +: collectedParts(orderNo)
      collectedParts += orderNo -> partsForOrder
      invoices += orderNo -> (invoices(orderNo) + price)
      val orderedItem = orders(orderNo)
      val isComplete = PackageValidators(orderedItem)
      if (isComplete(partsForOrder)) {
        assembly ! Package(orderNo, orderedItem, partsForOrder)
        collectedParts -= orderNo
      }
  }

  val acceptAssembledProducts: Receive = {
    case Assembled(orderNo, price, product) =>
      customers(orderNo) ! OrderComplete(product, invoices(orderNo) + price) // invoices += orderNo -> (invoices(orderNo) + price)
      customers -= orderNo
      orders -= orderNo
      invoices -= orderNo
  }
  
  private def nextLegWorker() = freeLegWorkers match {
    case worker :: remaining =>
      freeLegWorkers = remaining
      worker
    case Nil => context.actorOf(Master.props(classOf[Leg], PartBasePrice, ManufacturingTime))
  }

  private def nextChairWorker() = freeChairTopWorkers match {
    case worker :: remaining =>
      freeChairTopWorkers = remaining
      worker
    case Nil => context.actorOf(Master.props(classOf[ChairTop], PartBasePrice, ManufacturingTime))
  }

  private def nextTableTopWorker() = freeTableTopWorkers match {
    case worker :: remaining =>
      freeTableTopWorkers = remaining
      worker
    case Nil => context.actorOf(Master.props(classOf[TableTop], PartBasePrice, ManufacturingTime))
  }
}

/** Defines the messages of the contract of a [[net.nastich.factory.actor.Manufacturer]]. */
object Manufacturer {

  /** @see [[net.nastich.factory.actor.Manufacturer]] */
  def props(settings: Settings) = Props(new Manufacturer(settings))

  /**
   * This message should be sent to the [[net.nastich.factory.actor.Manufacturer]] actor to order an item.
   * If everything is intact, the Factory will respond with an [[net.nastich.factory.actor.Manufacturer.OrderComplete]].
   * 
   * @param item ordered item
   */
  case class Shop(item: Item)

  /**
   * This message is expected to be replied with to Shopping orders ([[net.nastich.factory.actor.Manufacturer.Shop]])
   */
  case class OrderComplete(item: Item, billAmount: BigDecimal)

}