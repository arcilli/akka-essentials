package part2.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "hi" => context.sender() ! "Hello, there" // replying to a message
      case message: String => println(s"[${context.self}] I have received $message")
      case number: Int => println(s"[${self.path}] I have received a NUMBER: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received a NUMBER: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "hi"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "sss") // I keep the original sender of the wirelessPhoneMessage
    }
  }

  val actorSystem = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // "!" <=> "tell" method
  // a) messages MUST BE immutable
  // b) messages MUST BE SERIALIZABLE

  // in practice, use case classes & case objects
  simpleActor ! 42

  case class SpecialMessage(contents: String)

  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context. self === `this` from OOP

  case class SendMessageToYourself(content: String)

  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it.")

  // 3 - actors can REPLY to messages
  val alice = actorSystem.actorOf(Props[SimpleActor], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "hi" // reply to "me"

  // 5 - forwarding message
  // D -> A -> B
  // forwarding = sending a message with the ORIGINAL sender
  case class WirelessPhoneMessage(content: String, destinationRef: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)
}