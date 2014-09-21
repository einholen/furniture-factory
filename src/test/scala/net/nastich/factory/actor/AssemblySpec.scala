package net.nastich.factory.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import net.nastich.factory.actor.Assembly._
import net.nastich.factory.actor.Manufacturer._
import org.scalatest.{OneInstancePerTest, Matchers, BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class AssemblySpec
    extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ImplicitSender
    with OneInstancePerTest {

  override protected def afterAll(): Unit = system.shutdown()

  val price = BigDecimal(10.0)

  val testChairPackageComplete = Seq(ChairSeat(1), ChairBack(2), ChairLeg(3), ChairLeg(4), ChairLeg(5), ChairLeg(6))
  val testTablePackageComplete = Seq(TableLeg(1), TableTop(2), TableLeg(3), TableLeg(4), TableLeg(5))

  "Assembly actor" should {

    "accept assembly requests for complete packages" in {
      val time = 250.millis
      val assembly = system.actorOf(Assembly.props(price, time))

      assembly ! Package(1L, Chair, testChairPackageComplete)
      expectNoMsg(time)
      expectMsg(Assembled(1L, price, Chair))

      assembly ! Package(1L, Table, testTablePackageComplete)
      expectNoMsg(time)
      expectMsg(Assembled(1L, price, Table))
    }

    "take predefined time to process each of requests" in {
      val time = 250.millis
      val assembly = system.actorOf(Assembly.props(price, time))

      assembly ! Package(1L, Chair, testChairPackageComplete)
      assembly ! Package(2L, Chair, testChairPackageComplete)

      expectNoMsg(time)
      expectMsg(Assembled(1L, price, Chair))

      expectNoMsg(time)
      expectMsg(Assembled(2L, price, Chair))
    }
    
    "reject incomplete packages" in {
      val time = 10.millis
      val assembly = system.actorOf(Assembly.props(price, time))
      assembly ! Package(1L, Chair, Seq.empty)
      expectMsg(ImproperPackage(1L))

      assembly ! Package(2L, Table, Seq.empty)
      expectMsg(ImproperPackage(2L))

      assembly ! Package(3L, Chair, Seq(ChairLeg(1), ChairLeg(2), ChairBack(3)))
      expectMsg(ImproperPackage(3L))

      assembly ! Package(4L, Table, Seq(TableLeg(1), TableTop(3)))
      expectMsg(ImproperPackage(4L))

      assembly ! Package(5L, Table, testTablePackageComplete :+ ChairLeg(10))
      expectMsg(ImproperPackage(5L))
    }
    

  }

}
