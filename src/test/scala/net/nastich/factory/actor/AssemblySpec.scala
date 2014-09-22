package net.nastich.factory.actor

import net.nastich.factory.actor.Assembly._
import net.nastich.factory.model._
import net.nastich.factory.common.TestKitWordSpec

import scala.concurrent.duration._

class AssemblySpec extends TestKitWordSpec("AssemblySpec") {

  val price = BigDecimal(10.0)

  val testChairPackageComplete = Seq(ChairSeat(), ChairBack(), ChairLeg(), ChairLeg(), ChairLeg(), ChairLeg())
  val testTablePackageComplete = Seq(TableLeg(), TableTop(), TableLeg(), TableLeg(), TableLeg())

  "Assembly actor" should {

    "accept assembly requests for complete packages" in {
      val time = 250.millis
      val assembly = system.actorOf(Assembly.props(price, time))

      assembly ! Package(1L, Chair, testChairPackageComplete)
      expectNoMsg(time mul 9 div 10)
      expectMsg(Assembled(1L, price, Chair))

      assembly ! Package(1L, Table, testTablePackageComplete)
      expectNoMsg(time mul 9 div 10)
      expectMsg(Assembled(1L, price, Table))
    }

    "take predefined time to process each of requests" in {
      val time = 250.millis
      val assembly = system.actorOf(Assembly.props(price, time))

      assembly ! Package(1L, Chair, testChairPackageComplete)
      assembly ! Package(2L, Chair, testChairPackageComplete)

      expectNoMsg(time mul 9 div 10)
      expectMsg(Assembled(1L, price, Chair))

      expectNoMsg(time mul 9 div 10)
      expectMsg(Assembled(2L, price, Chair))
    }
    
    "reject incomplete packages" in {
      val time = 10.millis
      val assembly = system.actorOf(Assembly.props(price, time))
      assembly ! Package(1L, Chair, Seq.empty)
      expectMsg(ImproperPackage(1L))

      assembly ! Package(2L, Table, Seq.empty)
      expectMsg(ImproperPackage(2L))

      assembly ! Package(3L, Chair, Seq(ChairLeg(), ChairLeg(), ChairBack()))
      expectMsg(ImproperPackage(3L))

      assembly ! Package(4L, Table, Seq(TableLeg(), TableTop()))
      expectMsg(ImproperPackage(4L))

      assembly ! Package(5L, Table, testTablePackageComplete :+ ChairLeg())
      expectMsg(ImproperPackage(5L))
    }
    

  }

}
