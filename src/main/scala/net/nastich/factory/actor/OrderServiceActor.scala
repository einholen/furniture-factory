package net.nastich.factory.actor

import akka.actor.{ActorLogging, Actor, Props}
import net.nastich.factory.OrderService

/**
 * Main application interface, serves HTTP requests for the orders.
 *
 * @param factoryProps Props of a Factory Actor that should process furniture orders for the clients. Expected
 *                     to be [[net.nastich.factory.actor.Manufacturer#props(net.nastich.factory.Settings)]]
 */
class OrderServiceActor(val factoryProps: Props) extends Actor with ActorLogging with OrderService {
  lazy val actorRefFactory = context
  def receive = runRoute(route)
}

object OrderServiceActor {
  def props(factoryActorProps: Props): Props = Props(classOf[OrderServiceActor], factoryActorProps)
}
