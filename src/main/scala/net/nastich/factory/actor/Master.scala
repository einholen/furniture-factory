package net.nastich.factory.actor

import akka.actor._
import net.nastich.factory.model.Part.PartType
import net.nastich.factory.model._

import scala.concurrent.duration.FiniteDuration
import scala.math.BigDecimal.RoundingMode

/**
 * This actor represents an "outsourcing" master that crafts [[net.nastich.factory.model.Part]]s of a
 * specific type. The master has two modes:
 *
 * $  ''Standby'': In this mode the master accepts messages of type [[net.nastich.factory.actor.Master.PartRequest]]
 *    holding a part type that should be the same that it's configured for (using
 *    [[net.nastich.factory.actor.Master#props]]). Upon receiving the message the master transitions to ''Working''
 *    state. As soon as a part is ready the master responds with a [[net.nastich.factory.actor.Master.PartComplete]]
 *    message which contains the produced part and the price to be paid. The price of a table leg is two times the 
 *    default price and the base price is raised by 5% after every 10 produced parts.
 * $ ''Working'': In this mode the master is working hard on producing a part and does not accept any messages.
 *
 * @author sena0713
 * @since 20.09.2014 
 */
class Master(partType: PartType, basePrice: BigDecimal, duration: FiniteDuration) extends Actor with ActorLogging {

  import context.dispatcher
  import net.nastich.factory.actor.Master._
  
  var price: BigDecimal = basePrice
  var producedAmount: Int = 0
  var requesters: Map[Long, ActorRef] = Map.empty

  def receive = {
    case PartRequest(orderNo, requestedType) if partType.isAssignableFrom(requestedType) =>
      log.debug(s"Master $logSelf received a Part request for a ${requestedType.getSimpleName} and is now working on it")
      context.become(working, discardOld = false)
      val part = CreatePart(requestedType)()
      requesters += orderNo -> sender()
      context.system.scheduler.scheduleOnce(duration, self, (orderNo, part))

    case PartRequest(orderNo, alienType) =>
      log.info(s"Master $logSelf received a part request for a wrong type $alienType")
      sender() ! UnrecognizedPartType(orderNo, partType)
  }

  val working: Receive = {
    case (orderNo: Long, part: Part) if sender() == self =>
      context.unbecome()
      producedAmount = producedAmount + 1
      if (producedAmount > 0 && producedAmount % 10 == 0)
        price = (price * 1.05).setScale(2, RoundingMode.HALF_UP)
      val finalPrice = part match {
        case TableLeg(_) => price * 2
        case _ => price
      }
      requesters(orderNo) ! PartComplete(orderNo, finalPrice, part)
      log.debug(s"Master $logSelf finished ${PartComplete(orderNo, finalPrice, part)} and is sending it back to ${requesters(orderNo)}")
      requesters -= orderNo

    case unhandled =>
      log.info(s"Master $logSelf received unhandled message $unhandled.")
  }

  private def logSelf = s"$self [works on ${partType.getSimpleName}, created $producedAmount parts, current price $price]"

}

/** Defines the messages of the contract of a [[net.nastich.factory.actor.Master]]. */
object Master {

  def props(partType: PartType, basePrice: BigDecimal, duration: FiniteDuration): Props =
    Props(new Master(partType, basePrice, duration))

  /**
   * Send to the Master to request a [[net.nastich.factory.model.Part]] from him. Expected responses to this message
   * are either [[PartComplete]] or [[UnrecognizedPartType]].
   *
   * @param orderNo order number to remember the requester while working
   * @param partType part type
   */
  case class PartRequest(orderNo: Long, partType: PartType)

  /**
   * A Master responds with this message when he's finished crafting a part and is ready to accept other orders.
   *
   * @param orderNo order number for the records
   * @param price price of the produced part
   * @param part produced part
   */
  case class PartComplete(orderNo: Long, price: BigDecimal, part: Part)

  /**
   * A Master responds with this message if he's requested to produce a Part he doesn
   *
   * @param orderNo order number
   * @param actualPartType the part type the master is configured with
   */
  case class UnrecognizedPartType(orderNo: Long, actualPartType: PartType)

  val CreatePart: Map[PartType, () => Part] = Map(
    classOf[TableLeg] -> (() => TableLeg()),
    classOf[TableTop] -> (() => TableTop()),
    classOf[ChairLeg] -> (() => ChairLeg()),
    classOf[ChairSeat] -> (() => ChairSeat()),
    classOf[ChairBack] -> (() => ChairBack()))

}
