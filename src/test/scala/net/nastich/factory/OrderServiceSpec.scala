package net.nastich.factory

import akka.actor.{Actor, Props}
import akka.event.{LoggingAdapter, NoLogging}
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.model._
import org.scalatest.{Matchers, WordSpec}
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.testkit._

class OrderServiceSpec extends WordSpec with OrderService with ScalatestRouteTest with Matchers {

  def actorRefFactory = system

  val log: LoggingAdapter = NoLogging

  val factoryProps: Props = Props(new Actor {
    def receive = {
      case Shop(item) => sender() ! OrderComplete(item, 100.0)
    }
  })

  "Routing" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> route ~> check {
        responseAs[String] should include("Hello! You can order")
        responseAs[String] should include("/order?item=chair")
        responseAs[String] should include("/order?item=table")
      }
    }

    "handle and acknowledge requests for chairs" in {
      Get("/order?item=chair") ~> route ~> check {
        responseAs[OrderComplete] shouldBe OrderComplete(Chair, 100.0)
      }
    }

    "handle and acknowledge requests for tables" in {
      Get("/order?item=table") ~> route ~> check {
        responseAs[OrderComplete] shouldBe OrderComplete(Table, 100.0)
      }
    }

    "leave GET requests to random paths unhandled" in {
      Seq(
        Get("/kermit") ~> route ~> check {
          handled shouldBe false
        },
        Get("/order") ~> route ~> check {
          handled shouldBe false
        },
        Get("/order?item=something_unknown") ~> route ~> check {
          responseAs[String] should include("Cannot handle item type")
        })
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }

}
