package net.nastich.factory.actor

import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.event.LoggingReceive
import net.nastich.factory.model.Part.PartType
import net.nastich.factory.model._

import scala.concurrent.duration._

/**
 * This actor represents the Assembly service of the factory. It has two modes:
 *
 * $ ''Standby'': In this mode the Assembly accepts [[net.nastich.factory.actor.Assembly.Package]]s of parts,
 *    validates their conformance with the predefined blueprints (a chair may only be assembled from 4 legs, a seat
 *    and a back, and a table should be assembled from 4 legs and a tabletop) and either produces products
 *    (eventually responding [[net.nastich.factory.actor.Assembly.Assembled]]) or rejects the package, responding with
 *    an [[net.nastich.factory.actor.Assembly.ImproperPackage]].
 * $ ''Busy'': While an Assembly is working on a product, any other orders are stashed and will be processed as
 *    soon as the work is done, no additional checks are needed from the outside.
 *
 * @author sena0713
 * @since 20.09.2014 
 */
class Assembly(price: BigDecimal, time: FiniteDuration) extends Actor with Stash with ActorLogging {
  import context.dispatcher
  import Assembly._

  def receive = standby

  val standby: Receive = LoggingReceive {
    case request @ Package(orderNo, product, parts) =>
      val isComplete = PackageValidators.getOrElse(product, (_: Seq[Part]) => false)
      val completePackage = parts.nonEmpty && isComplete(parts)
      if (completePackage) {
        val manufacturer = sender()
        context become busy
        log.debug(s"Assembly $self is now busy and is going to be working for at least $time")
        context.system.scheduler.scheduleOnce(time) {
          manufacturer ! Assembled(orderNo, price, product)
          self ! WorkDone
        }
      } else {
        sender() ! ImproperPackage(orderNo)
      }
  }

  lazy val busy: Receive = LoggingReceive {
    case request: Package => stash()
    case WorkDone =>
      context become standby
      unstashAll()
  }
}

/** Defines the messages of the contract of an [[net.nastich.factory.actor.Assembly]]. */
object Assembly {

  def props(price: BigDecimal, time: FiniteDuration): Props = Props(new Assembly(price, time))

  /**
   * Send to the Assembly to create a product from parts. Expected responses to this message are either [[Assembled]]
   * or [[ImproperPackage]] if the parts do not conform to the blueprints ([[PackageValidators]] is used to check).
   *
   * @param orderNo order number to store the original sender
   * @param requested the item type that's requested for assembly
   * @param parts parts for the item, should be complete by [[PackageValidators]]
   */
  case class Package(orderNo: Long, requested: Item, parts: Seq[Part])

  /**
   * The Assembly responds with this message in case it receives a package which is incomplete and thus can't be used
   * to produce an item.
   *
   * @param orderNo order number for the records of the sender
   */
  case class ImproperPackage(orderNo: Long)

  /**
   * The Assembly responds with this message when it's finished assembling a product.
   *
   * @param orderNo order number for the records of the sender
   * @param price cost of the operation (the invoice)
   * @param product assembled product type
   */
  case class Assembled(orderNo: Long, price: BigDecimal, product: Item)

  /** (internal for Assembly) Signalizes that the work is done and that all pending messages should be unstashed. */
  private[Assembly] case object WorkDone

  private val TableBlueprint: Seq[PartType] = sortSeqOfClasses[Part](Seq(
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableLeg],
    classOf[TableTop]))

  private val ChairBlueprint: Seq[PartType] = sortSeqOfClasses[Part](Seq(
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairLeg],
    classOf[ChairSeat],
    classOf[ChairBack]))

  /**
   * This Map should be used as a container of Blueprints to validate that products of a specific type can be produced
   * from collected Seqs of Parts.
   */
  val PackageValidators: Map[Item, Seq[Part] => Boolean] = Map(
    Chair -> validatePartsAgainst(ChairBlueprint),
    Table -> validatePartsAgainst(TableBlueprint))

  private def validatePartsAgainst(partTypes: Seq[PartType]): (Seq[Part] => Boolean) =
    parts => sortSeqOfClasses[Part](parts.map(_.getClass)).sameElements(partTypes)
  
  private def sortSeqOfClasses[T](seq: Seq[Class[_ <: T]]): Seq[Class[_ <: T]] =
    seq.sortWith((c1, c2) => c1.getSimpleName.compareTo(c2.getSimpleName) < 0)

}
