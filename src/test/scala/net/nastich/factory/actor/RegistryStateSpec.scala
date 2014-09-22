package net.nastich.factory.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, EventFilter, TestActorRef}
import com.typesafe.config.ConfigFactory
import net.nastich.factory.actor.Registry._
import net.nastich.factory.common.TestKitWordSpec
import net.nastich.factory.model.{Table, Chair, TableTop, ChairLeg}
import org.scalatest._

import scala.concurrent.duration._
/**
 *
 * @author sena0713
 * @since 22.09.2014 
 */
class RegistryStateSpec extends TestKitWordSpec("RegistryStateSpec") with BeforeAndAfter {

  var testRegistry: TestActorRef[Registry] = _
  var registryActor: Registry = _

  before {
    testRegistry = TestActorRef[Registry]
    registryActor = testRegistry.underlyingActor
    registryActor.printSchedule.cancel()
  }

  "Registry's state" should {
    
    "correctly increment amount of orders taken for [RegisterIncomingOrder]" in {
      testRegistry ! RegisterIncomingOrder
      registryActor.amountOfOrders shouldBe 1

      testRegistry ! RegisterIncomingOrder
      registryActor.amountOfOrders shouldBe 2
    }

    "correctly update amount of parts and total costs for [RegisterSoldPart]" in {
      testRegistry ! RegisterSoldPart(classOf[ChairLeg], 10.00)
      testRegistry ! RegisterSoldPart(classOf[ChairLeg], 10.05)
      testRegistry ! RegisterSoldPart(classOf[TableTop], 20.00)

      registryActor.partsSold should contain only (classOf[ChairLeg] -> 2, classOf[TableTop] -> 1)
      registryActor.totalCost shouldBe BigDecimal(40.05)
    }

    "correctly update amount of items, total costs and finished orders for [RegisterAssembly]" in {
      testRegistry ! RegisterAssembly(Chair, 10.00)
      testRegistry ! RegisterAssembly(Chair, 10.00)
      testRegistry ! RegisterAssembly(Table, 10.00)

      registryActor.itemsSold should contain only (Chair -> 2, Table -> 1)
      registryActor.totalCost shouldBe BigDecimal(30.00)
      registryActor.finishedOrders shouldBe 3
    }
  }

}
