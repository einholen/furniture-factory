package net.nastich.factory.common

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.Config
import org.scalatest._

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
abstract class TestKitWordSpec(name: String, config: Config = null)
    extends TestKit(ActorSystem(name, Option(config)))
    with WordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfter
    with Matchers
    with ImplicitSender
    with OneInstancePerTest {

  override protected def afterAll(): Unit = system.shutdown()

}
