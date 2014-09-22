package net.nastich.factory.actor

import akka.actor.{ActorSystem, Props}
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
class RegistrySpec
    extends TestKit(ActorSystem("testSystem",
      ConfigFactory.parseString("""akka.loggers = [akka.testkit.TestEventListener]""")))
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with OneInstancePerTest {

  override protected def afterAll(): Unit = system.shutdown()

  "Registry's state" should {
    "correctly increment amount of orders taken for [RegisterIncomingOrder]" in {
      val registry = TestActorRef[Registry]
      val registryActor = registry.underlyingActor

      registry ! RegisterIncomingOrder
      registryActor.amountOfOrders shouldBe 1

      registry ! RegisterIncomingOrder
      registryActor.amountOfOrders shouldBe 2
    }

    "correctly update amount of parts and total costs for [RegisterSoldPart]" in {
      val registry = TestActorRef[Registry]
      val registryActor = registry.underlyingActor

      registry ! RegisterSoldPart(classOf[ChairLeg], 10.00)
      registry ! RegisterSoldPart(classOf[ChairLeg], 10.05)
      registry ! RegisterSoldPart(classOf[TableTop], 20.00)

      registryActor.partsSold should contain only (classOf[ChairLeg] -> 2, classOf[TableTop] -> 1)
      registryActor.totalCost shouldBe BigDecimal(40.05)
    }

    "correctly update amount of items, total costs and finished orders for [RegisterAssembly]" in {
      val registry = TestActorRef[Registry]
      val registryActor = registry.underlyingActor

      registry ! RegisterAssembly(Chair, 10.00)
      registry ! RegisterAssembly(Chair, 10.00)
      registry ! RegisterAssembly(Table, 10.00)

      registryActor.itemsSold should contain only (Chair -> 2, Table -> 1)
      registryActor.totalCost shouldBe BigDecimal(30.00)
      registryActor.finishedOrders shouldBe 3
    }

    "print diagnostic information upon [PrintDiagnostic]" in {
      val registry = system.actorOf(Props[Registry])

      EventFilter.info(start = "=== Furniture Factory stats:", occurrences = 1) intercept {
        registry ! PrintDiagnostic
      }
    }

    "print diagnostic information periodically on its own" in {
      val registry = system.actorOf(Props[Registry])

      EventFilter.info(start = "=== Furniture Factory stats:", occurrences = 1) intercept {
        expectNoMsg(2.seconds)
      }
    }
  }

}
