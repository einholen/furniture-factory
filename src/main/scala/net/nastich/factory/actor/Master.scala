package net.nastich.factory.actor

import akka.actor.{Actor, ActorLogging, Stash}
import net.nastich.factory.actor.Manufacturer.Part

/**
 *
 * @author sena0713
 * @since 20.09.2014 
 */
abstract class Master extends Actor with ActorLogging with Stash {
  
  var price: BigDecimal
  var partsAndPrices: Map[Part, BigDecimal]

}

object Master {

  case class PartRequest(orderNo: Long, part: Class[_ <: Part])
  case class PartComplete(orderNo: Long, part: Part)

}
