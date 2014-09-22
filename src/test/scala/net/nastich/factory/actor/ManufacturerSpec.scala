package net.nastich.factory.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import net.nastich.factory.Settings
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.common.TestKitWordSpec
import net.nastich.factory.model.{Chair, Table}
import org.scalatest.{OneInstancePerTest, Matchers, BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class ManufacturerSpec extends TestKitWordSpec("ManufacturerSpec",
    ConfigFactory.load("test").withFallback(ConfigFactory.load("application"))) {

  val log = system.log
  val settings = Settings(system.settings.config)
  import settings._

  "The Factory (manufacturer actor)" should {
    "produce chairs and it should take at least assembly time" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings), "test-factory")

      manufacturer ! Shop(Chair)
      expectNoMsg(ManufacturingTime + AssemblyDuration)
      expectMsgPF() { case OrderComplete(Chair, _) => () }
    }

    "produce tables and it should take at least assembly time" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings), "test-factory")

      manufacturer ! Shop(Table)
      expectNoMsg(ManufacturingTime + AssemblyDuration)
      expectMsgPF() { case OrderComplete(Table, _) => () }
    }

    "be able to produce many tables" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings), "test-factory")

      for (i <- 1 to 1000) manufacturer ! Shop(Table)
      within (15.seconds) {
        for (i <- 1 to 1000) {
          expectMsgPF() { case OrderComplete(Table, _) => () }
          if (i % 100 == 0) system.log.debug(s"Received $i / 1000 responses.")
        }
      }
    }

    "assemble up to K products simultaneously" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings), "test-factory")

      val expectedNoResponse = ManufacturingTime + AssemblyDuration
      for (i <- 1 to AssemblyCapacity) manufacturer ! Shop(if (i % 2 == 0) Chair else Table)
      log.debug(s"assemble K products simultaneously -- Sent Shop messages x$AssemblyCapacity to the manufacturer, " +
        s"now expecting it not to respond for at least $expectedNoResponse")

      val msg = expectMsgType[OrderComplete]
      log.debug(s"assemble K products simultaneously -- Received $msg")
      within(20.millis) {
        for (i <- 2 to AssemblyCapacity)  {
          val msg = expectMsgType[OrderComplete]
          log.debug(s"assemble K products simultaneously -- Received $msg")
        }
      }
    }
  }

}
