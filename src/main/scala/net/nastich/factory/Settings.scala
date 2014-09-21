package net.nastich.factory

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration._

/**
 *
 * @author sena0713
 * @since 20.09.2014 
 */
case class Settings(config: Config) {

  import config._

  private val componentPrefix = "net.nastich.factory.component"
  private val assemblyPrefix = "net.nastich.factory.assembly"

  val ChairLegManufacturingTime = getMillis(s"$componentPrefix.chair.leg.manufacturing-time")
  val ChairSeatManufacturingTime = getMillis(s"$componentPrefix.chair.seat.manufacturing-time")
  val ChairBackManufacturingTime = getMillis(s"$componentPrefix.chair.back.manufacturing-time")
  val TableLegManufacturingTime = getMillis(s"$componentPrefix.table.leg.manufacturing-time")
  val TableTopManufacturingTime = getMillis(s"$componentPrefix.table.top.manufacturing-time")

  val ChairLegBasePrice = getPrice(s"$componentPrefix.chair.leg.base-price")
  val ChairSeatBasePrice = getPrice(s"$componentPrefix.chair.seat.base-price")
  val ChairBackBasePrice = getPrice(s"$componentPrefix.chair.back.base-price")
  val TableLegBasePrice = getPrice(s"$componentPrefix.table.leg.base-price")
  val TableTopBasePrice = getPrice(s"$componentPrefix.table.top.base-price")

  val AssemblyDuration = getMillis(s"$assemblyPrefix.assembly-time")
  val AssemblyCapacity = getInt(s"$assemblyPrefix.nr-of-instances")
  val AssemblyPrice = BigDecimal(getString(s"$assemblyPrefix.price"))

  private def getMillis(path: String): FiniteDuration = getDuration(path, MILLISECONDS).millis
  private def getPrice(path: String): BigDecimal = BigDecimal(getString(path))

}