package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello there also" // replying to a message
      case message: String => println(s"[simple actor ${self}] I have received $message")
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(e) => println(s"[simple actor] I have received SPECIAL MESSAGE: $e")
      case SendMessageToYourSelf(content) =>
        self ! content
      case SayHiTo(reference) => reference ! "Hi" // alice is being passed as the sender
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender of the wireless phone message
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello Actor!!"

  // 1 - messages can be of any type
  // Conditions:
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE


  // in practice, use case classes & case objects
  simpleActor.!(42)

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context & about themselves
  // context.self is the equivalent of "this" in OOP

  case class SendMessageToYourSelf(content: String)
  simpleActor ! SendMessageToYourSelf("I am an actor & I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi" // reply to "me"

  // 5 - forwarding messages
  // D -> A -> B
  // forwarding = sending a message with storing of the original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hii", bob)
}
