package part3

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A simple actor" should {
    "send back the same message" in {


      object BasicSpec {
        class SimpleActorPart3 extends Actor {
          override def receive: Receive = {
            case message => sender() ! message
          }
        }
      }

      // testing scenario

      val echoActor = system.actorOf(Props[BasicSpec.SimpleActorPart3], "a")
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)
    }

    "do another thing" in {
      // testing scenario
    }
  }
}
