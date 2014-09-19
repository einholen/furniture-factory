package com.example

import akka.actor.{Actor, Props}
import net.nastich.factory.OrderService
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

class OrderServiceSpec extends Specification with Specs2RouteTest with OrderService {
  def actorRefFactory = system
  val factoryProps: Props = Props(new Actor {
    def receive = {
      case _ =>
    }
  })
  
  "Routing" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> route ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> route ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }

}
