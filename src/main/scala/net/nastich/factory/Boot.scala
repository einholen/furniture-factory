package net.nastich.factory

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import net.nastich.factory.actor.{Manufacturer, OrderServiceActor}
import spray.can.Http

import scala.concurrent.duration._

/** Application entry point */
object Boot extends App {
  implicit val system = ActorSystem("furniture-factory")
  implicit val timeout = Timeout(5.seconds)
  val settings = Settings(system.settings.config)
  val service = system.actorOf(OrderServiceActor.props(Manufacturer.props(settings)), "order-service")

  val log = system.log
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}