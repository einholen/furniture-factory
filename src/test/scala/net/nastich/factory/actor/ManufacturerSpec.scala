package net.nastich.factory.actor

import com.typesafe.config.ConfigFactory
import net.nastich.factory.Settings
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.common.TestKitWordSpec

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
  }

}
