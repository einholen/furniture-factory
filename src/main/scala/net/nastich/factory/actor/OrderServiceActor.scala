package net.nastich.factory.actor

import akka.actor.{ActorLogging, Actor, Props}
import net.nastich.factory.OrderService

/**
 * Main application interface, serves HTTP requests for the furniture.
 * @param factoryProps Props of an Actor that should process furniture orders for the clients
 */
class OrderServiceActor(val factoryProps: Props) extends Actor with ActorLogging with OrderService {
  lazy val actorRefFactory = context
  def receive = runRoute(route)
}

object OrderServiceActor {
  def props(factoryActorProps: Props): Props = Props(classOf[OrderServiceActor], factoryActorProps)
}
