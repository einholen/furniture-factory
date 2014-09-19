package net.nastich.factory

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import net.nastich.factory.actor.Manufacturer
import net.nastich.factory.actor.Manufacturer.{Item, OrderComplete}
import spray.http.MediaTypes._
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.routing._

import scala.concurrent.duration._

trait OrderService extends HttpService { this: ActorLogging =>

  def actorRefFactory: ActorContext

  val factoryProps: Props
//  val manufacturer: ActorRef = actorRefFactory.actorOf(factoryProps, "furniture-factory")
  lazy val manufacturer: ActorRef = actorRefFactory.actorOf(Manufacturer.props, "furniture-factory")

  implicit val executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5.seconds)

  implicit val itemFormat = new JsonFormat[Item] {
    override def write(item: Item): JsValue = JsString(item.text)
    override def read(json: JsValue): Item = json match {
      case JsString(Item(item)) => item
      case _ => deserializationError("Cannot fetch item type from JSON")
    }
  }

  implicit val orderFormat = jsonFormat2(OrderComplete)

  val route =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                Hello! You can order a <a href="/order?item=chair">chair</a> or a <a href="/order?item=table">table</a>.
              </body>
            </html>
          }
        }
      }
    } ~
    (path("order") & parameter('item)) { (itemName: String) =>
      get {
        complete {
          import net.nastich.factory.actor.Manufacturer._
          import spray.httpx.SprayJsonSupport._
          log.info(s"Received request. itemName = $itemName")
          itemName match {
            case Item(actualItem) =>
              log.info(s"Asking manufacturer $manufacturer to produce $actualItem")
              val future = (manufacturer ? Shop(actualItem)).mapTo[OrderComplete]//.map(msg => msg.toJson)
              future.onComplete(res => log.info(s"Result: $res"))
              future
            case unknown => s"Cannot handle item type: $unknown"
          }
        }
      }
    }
}