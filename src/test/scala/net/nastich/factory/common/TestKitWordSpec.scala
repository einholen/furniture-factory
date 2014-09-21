package net.nastich.factory.common

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{OneInstancePerTest, Matchers, BeforeAndAfterAll, WordSpecLike}

/**
 * Base class to extend for akka actor unit testing. Mixes in the following modules:
 *
 * akka-testkit -> TestKit, ImplicitSender
 *
 * scalatest -> WordSpec, Matchers, BeforeAndAfterAll, OneInstancePerTest
 *
 * @author sena0713
 * @since 21.09.2014 
 */
abstract class TestKitWordSpec
  extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ImplicitSender
  with OneInstancePerTest {

  override protected def afterAll(): Unit = system.shutdown()

}
