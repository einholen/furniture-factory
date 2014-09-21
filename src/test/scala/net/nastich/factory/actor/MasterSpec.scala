package net.nastich.factory.actor

import net.nastich.factory.actor.Manufacturer._
import net.nastich.factory.actor.Master._
import net.nastich.factory.common.TestKitWordSpec
import org.scalatest.Assertions

import scala.concurrent.duration._
import scala.BigDecimal._

class MasterSpec extends TestKitWordSpec with Assertions {

  val defaultPrice = BigDecimal(10)

  "A Master" should {
    "reject parts that it doesn't know" in {
      val master = system.actorOf(Master.props(classOf[ChairSeat], defaultPrice, 1.milli))

      master ! PartRequest(1L, classOf[TableTop])
      expectMsg(UnrecognizedPartType(1L, classOf[ChairSeat]))
    }

    "accept parts that are subclasses of what's configured" in {
      val master = system.actorOf(Master.props(classOf[ChairTop], defaultPrice, 1.milli))

      master ! PartRequest(1L, classOf[ChairSeat])
      expectMsgPF() { case PartComplete(1L, `defaultPrice`, ChairSeat(_)) => }

      master ! PartRequest(2L, classOf[ChairBack])
      expectMsgPF() { case PartComplete(2L, `defaultPrice`, ChairBack(_)) => }
    }

    "take time to produce parts" in {
      val duration = 50.millis
      val master = system.actorOf(Master.props(classOf[ChairLeg], defaultPrice, duration))
      master ! PartRequest(1L, classOf[ChairLeg])
      expectNoMsg(duration mul 4 div 5)
      expectMsgPF() { case PartComplete(1L, `defaultPrice`, ChairLeg(_)) => () }
    }

    "produce one part at a time" in {
      val duration = 50.millis
      val master = system.actorOf(Master.props(classOf[ChairBack], defaultPrice, duration))

      master ! PartRequest(1L, classOf[ChairBack])
      master ! PartRequest(2L, classOf[ChairBack])
      within(duration * 2) {
        expectMsgPF() { case PartComplete(1L, `defaultPrice`, ChairBack(_)) => () }
        expectNoMsg()
      }
    }

    "raise costs with every 10th item" in {
      val master = system.actorOf(Master.props(classOf[TableTop], 100, 1.milli))

      val expectedPrices: Vector[BigDecimal] = Vector(100, 105, 110.25, 115.76)
      for (i <- 1 until 40) {
        val expectedPrice = expectedPrices(i / 10)

        master ! PartRequest(i, classOf[TableTop])
        expectMsgPF() {
          case PartComplete(`i`, price, TableTop(_)) =>
            if (price != expectedPrice) fail(s"On order #$i expected price $expectedPrice but received $price.")
        }
      }
    }

  }

}
