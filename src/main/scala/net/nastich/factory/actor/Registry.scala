package net.nastich.factory.actor

import akka.actor.FSM.->
import akka.actor.{Cancellable, ActorLogging, Actor}
import net.nastich.factory.actor.Registry.{RegisterSoldPart, PrintDiagnostic}
import net.nastich.factory.model.Item
import net.nastich.factory.model.Part.PartType

import scala.concurrent.duration._

/**
 * This is a registry which has mainly diagnostic purposes - it periodically prints out to console current
 * state of affairs: how many orders are taken, how many parts and items have been sold and for how much money.
 * The Registry accepts the following messages: [[net.nastich.factory.actor.Registry.RegisterIncomingOrder]],
 * [[net.nastich.factory.actor.Registry.RegisterSoldPart]], [[net.nastich.factory.actor.Registry.RegisterAssembly]].
 * The Registry periodically prints the current state of affairs, this printing can, however, be triggered manually
 * by sending [[net.nastich.factory.actor.Registry.PrintDiagnostic]] message.
 *
 * @author sena0713
 * @since 22.09.2014 
 */
class Registry extends Actor with ActorLogging {

  import Registry._

  var amountOfOrders: Long = _
  var finishedOrders: Long = _
  var partsSold: Map[PartType, Long] = Map.empty.withDefaultValue(0)
  var itemsSold: Map[Item, Long] = Map.empty.withDefaultValue(0)
  var totalCost: BigDecimal = 0.0

  import context.dispatcher
  var printSchedule: Cancellable = _

  override def preStart() = {
    printSchedule = context.system.scheduler.schedule(1.second, 10.seconds) {
      self ! PrintDiagnostic
    }
  }
  
  override def postStop() = printSchedule.cancel()

  def receive = {
    case PrintDiagnostic =>
      log.info(s"""
           |--- Furniture Factory stats:
           |  Orders taken: $amountOfOrders
           |  Finished orders: $finishedOrders
           |  Parts sold: $partsSold
           |  Assembled items sold: $itemsSold
           |  Total cost of works: $totalCost
         """.stripMargin)

    case RegisterIncomingOrder =>
      amountOfOrders += 1

    case RegisterSoldPart(partType, cost) =>
      partsSold += partType -> (partsSold(partType) + 1)
      totalCost += cost

    case RegisterAssembly(item, cost) =>
      itemsSold += item -> (itemsSold(item) + 1)
      totalCost += cost
      finishedOrders += 1
  }

}

/** Defines the messages of the contract of a [[net.nastich.factory.actor.Registry]]. */
object Registry {

  /** Send this message to register that a new order has been taken. */
  case object RegisterIncomingOrder

  /**
   * Send this message to register that a part has been acquired from a Master.
   *
   * @param partType type of the part
   * @param cost cost as in the Master's invoice
   */
  case class RegisterSoldPart(partType: PartType, cost: BigDecimal)

  /**
   * Send this message to register that an Item has been assembled, thus completing an order.
   *
   * @param item item type that was assembled
   * @param cost cost of assembly works
   */
  case class RegisterAssembly(item: Item, cost: BigDecimal)

  /** Upon receiving this message the Registry prints diagnostic information to the log */
  case object PrintDiagnostic

}
