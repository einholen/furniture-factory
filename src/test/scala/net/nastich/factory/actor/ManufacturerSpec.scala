package net.nastich.factory.actor

import com.typesafe.config.ConfigFactory
import net.nastich.factory.Settings
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.common.TestKitWordSpec

import scala.concurrent.duration._

class ManufacturerSpec extends TestKitWordSpec {

  val applicationConfig = ConfigFactory.load("application")
  val settings = Settings(ConfigFactory.load("test").withFallback(applicationConfig))
  import settings._

  "The Factory (manufacturer actor)" should {
    "produce chairs and it should take at least assembly time" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings))

      manufacturer ! Shop(Chair)
      expectNoMsg(ManufacturingTime + AssemblyDuration)
      expectMsgPF() { case OrderComplete(Chair, _) => () }
    }

    "produce tables and it should take at least assembly time" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings))

      manufacturer ! Shop(Table)
      expectNoMsg(ManufacturingTime + AssemblyDuration)
      expectMsgPF() { case OrderComplete(Table, _) => () }
    }

    "be able to produce many tables" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings))

      for (i <- 1 to 1000) manufacturer ! Shop(Table)
      within (10.seconds) {
        for (i <- 1 to 1000) {
          expectMsgPF() { case OrderComplete(Table, _) => () }
          if (i % 100 == 0) system.log.info(s"Received $i / 10000 responses.")
        }
      }
    }
  }

}
