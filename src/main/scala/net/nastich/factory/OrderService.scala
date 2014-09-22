package net.nastich.factory

import akka.actor._
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.model._
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.routing._

import scala.concurrent.duration._

/**
 * Defines the HTTP routing of the application.
 */
trait OrderService extends HttpService {

  def log: LoggingAdapter

  val factoryProps: Props

  lazy val manufacturer: ActorRef = actorRefFactory.actorOf(factoryProps, "furniture-factory")

  implicit lazy val executionContext = actorRefFactory.dispatcher

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
        respondWithMediaType(`application/json`) {
          import net.nastich.factory.actor.Manufacturer._
          itemName match {
            case Item(actualItem) => complete {
              log.debug(s"Ordering manufacturer to produce $actualItem")
              val future = (manufacturer ? Shop(actualItem)).mapTo[OrderComplete]
              future
            }
            case unknown => complete {
              StatusCodes.PreconditionFailed -> Map("message" -> s"Cannot handle item type: $unknown")
            }
          }
        }
      }
    }
}