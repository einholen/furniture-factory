package net.nastich.factory.actor

import akka.actor.{Actor, ActorLogging, Props, Stash}
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

  import context.dispatcher
  import net.nastich.factory.actor.Assembly._

  def receive = standby

  val standby: Receive = {
    case Package(orderNo, product, parts) =>
      val isComplete = PackageValidators.getOrElse(product, (_: Seq[Part]) => false)
      val completePackage = parts.nonEmpty && isComplete(parts)
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
  
  def sortSeqOfClasses[T](seq: Seq[Class[_ <: T]]): Seq[Class[_ <: T]] =
    seq.sortWith((c1, c2) => c1.getSimpleName.compareTo(c2.getSimpleName) < 0)

  private val tableParts: Seq[Class[_ <: Part]] = sortSeqOfClasses[Part](Seq(
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableTop]))

  private val chairParts: Seq[Class[_ <: Part]] = sortSeqOfClasses[Part](Seq(
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairSeat],
    classOf[ChairBack]))
  
  val PackageValidators: Map[Item, Seq[Part] => Boolean] = Map(
    Chair -> validatePartsAgainst(chairParts),
    Table -> validatePartsAgainst(tableParts))
  
  private def validatePartsAgainst(partTypes: Seq[Class[_ <: Part]]): (Seq[Part] => Boolean) =
    parts => sortSeqOfClasses[Part](parts.map(_.getClass)).sameElements(partTypes)

}
