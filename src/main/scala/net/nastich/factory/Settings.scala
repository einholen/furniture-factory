package net.nastich.factory

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import net.nastich.factory.actor.Manufacturer.ChairLeg
import net.nastich.factory.actor.Manufacturer.Part.PartType

import scala.concurrent.duration._

/**
 *
 * @author sena0713
 * @since 20.09.2014 
 */
case class Settings(config: Config) {

  import config._

  private val workerPrefix = "net.nastich.factory.worker"
  private val assemblyPrefix = "net.nastich.factory.assembly"

  val ManufacturingTime = getMillis(s"$workerPrefix.manufacturing-time")
  val PartBasePrice = getPrice(s"$workerPrefix.base-price")

  val AssemblyDuration = getMillis(s"$assemblyPrefix.assembly-time")
  val AssemblyCapacity = getInt(s"$assemblyPrefix.nr-of-instances")
  val AssemblyPrice = BigDecimal(getString(s"$assemblyPrefix.price"))

  private def getMillis(path: String): FiniteDuration = getDuration(path, MILLISECONDS).millis
  private def getPrice(path: String): BigDecimal = BigDecimal(getString(path))

}