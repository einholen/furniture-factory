package net.nastich.factory.actor

import akka.actor.{Props, Actor, ActorLogging, Stash}
import net.nastich.factory.actor.Manufacturer._

import scala.concurrent.duration._

/**
 * This actor represents the Assembly service of the factory. It accepts packages of parts and produces
 * a product from them.
 *
 * @author sena0713
 * @since 20.09.2014 
 */
class Assembly(price: BigDecimal, time: FiniteDuration) extends Actor with Stash with ActorLogging {

  import Assembly._
  import context.dispatcher

  def receive = standby

  val standby: Receive = {
    case Package(orderNo, product, parts) =>
      val partTypes = parts.map(_.getClass)
      val blueprint: Seq[Class[_ <: Part]] = partBlueprints.getOrElse(product, Seq.empty)
      val completePackage = parts.nonEmpty && (blueprint sameElements partTypes)
      if (completePackage) {
        val manufacturer = sender()
        context become busy
        context.system.scheduler.scheduleOnce(time) {
          manufacturer ! Assembled(orderNo, price, product)
          self ! WorkDone
        }
      } else {
        sender() ! ImproperPackage(orderNo)
      }
  }

  lazy val busy: Receive = {
    case request: Package => stash()
    case WorkDone =>
      context become standby
      unstashAll()
  }

}

object Assembly {

  def props(price: BigDecimal, time: FiniteDuration): Props = Props(new Assembly(price, time))
  
  case class Package(orderNo: Long, requested: Item, parts: Seq[Part])
  case class ImproperPackage(orderNo: Long)
  case class Assembled(orderNo: Long, price: BigDecimal, product: Manufacturer.Item)
  private[Assembly] case object WorkDone

  val tableParts: Seq[Class[_ <: Part]] = Seq(
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableTop])

  val chairParts: Seq[Class[_ <: Part]] = Seq(classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairSeat],
    classOf[ChairBack])
  
  val partBlueprints: Map[Item, Seq[Class[_ <: Part]]] = Map(Chair -> chairParts, Table -> tableParts)

}
