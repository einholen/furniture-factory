package net.nastich.factory.actor

import akka.actor.{ActorRef, Props}
import akka.testkit.{TestActorRef, EventFilter}
import com.typesafe.config.ConfigFactory
import net.nastich.factory.actor.Registry.PrintDiagnostic
import net.nastich.factory.common.TestKitWordSpec
import org.scalatest.BeforeAndAfter

import scala.concurrent.duration._

class RegistrySpec extends TestKitWordSpec("RegistryStateSpec",
      ConfigFactory.parseString("""akka.loggers = [akka.testkit.TestEventListener]"""))
    with BeforeAndAfter {

  var registry: TestActorRef[Registry] = _

  before {
    registry = TestActorRef[Registry]
  }

  after {
    system.stop(registry)
  }

  "Registry Actor" should {
    "print diagnostic information upon [PrintDiagnostic]" in {
      registry.underlyingActor.printSchedule.cancel()
      EventFilter.info(pattern = ".*Furniture Factory stats.*", occurrences = 1) intercept {
        registry ! PrintDiagnostic
      }
    }

    "print diagnostic information periodically on its own" in {
      EventFilter.info(pattern = ".*Furniture Factory stats.*", occurrences = 1) intercept {
        expectNoMsg(2.seconds)
      }
    }
  }

}
