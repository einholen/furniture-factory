package net.nastich.factory.actor

import com.typesafe.config.ConfigFactory
import net.nastich.factory.Settings
import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.common.TestKitWordSpec

class ManufacturerSpec extends TestKitWordSpec {

  val applicationConfig = ConfigFactory.load("application")
  val settings = Settings(ConfigFactory.load("test").withFallback(applicationConfig))
  import settings._

  val maxManufacturingDurations = Map(
    Chair -> (ChairBackManufacturingTime max ChairLegManufacturingTime max ChairSeatManufacturingTime),
    Table -> (TableLegManufacturingTime max TableTopManufacturingTime)
  )

  "The Factory (manufacturer actor)" should {
    "produce chairs" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings))
      val maxDuration = (AssemblyDuration + maxManufacturingDurations(Chair)) * 1.5
      manufacturer ! Shop(Chair)
      expectMsgPF(maxDuration) { case OrderComplete(Chair, _) => () }
    }

    "produce tables" in {
      val manufacturer = system.actorOf(Manufacturer.props(settings))
      val maxDuration = (AssemblyDuration + maxManufacturingDurations(Table)) * 1.5
      manufacturer ! Shop(Table)
      expectMsgPF(maxDuration) { case OrderComplete(Table, _) => () }
    }
  }

}
