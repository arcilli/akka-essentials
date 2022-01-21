package part2.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2.actors.ActorsCapabilities.Counter.{Decrement, Increment, Print}
import part2.actors.ActorsCapabilities.Person.LiveTheLife
import part2actors.ActorCapabilities.BankAccount
import part2actors.ActorCapabilities.BankAccount.{Deposit, Statement, TransactionFailure, TransactionSuccess, Withdraw}

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

  /**
   * Exercises
   *
   * 1. a. Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   *
   * 2. a. Bank account as an actor
   * receives
   *  - Deposit an amount
   *  - Withdraw an amount
   *  - Statement
   *
   * replies with
   *  - a Success/Failure
   *
   * - interact with some other kind of actor
   */

  // "DOMAIN" of the counter
  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {

    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => print(f"[counter] My current count is ${count}")
    }

    // bank account
    object BankAccount {
      case class Deposit(amount: Int)

      case class Withdraw(amount: Int)

      case object Statements

      case class TransactionSuccess(message: String)

      case class TransactionFailure(reason: String)
    }

    // bank account
    class BankAccount extends Actor {
      var funds = 0

      override def receive: Receive = {
        case Deposit(amount) =>
          if (amount < 0) sender() ! TransactionFailure("Invalid deposit amount")
          else {
            funds += amount
            sender() ! TransactionSuccess(s"Successfully deposited ${amount}")
          }

        case Withdraw(amount) =>
          if (amount > funds) sender() ! TransactionFailure("Insufficient funds")
          else {
            funds -= amount
            sender() ! TransactionSuccess(s"Successfully withdrew ${amount}")
          }

        case Statement => sender() ! s"Your balance is $funds"
      }
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {

    import Person._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement

      case message => println(message.toString)
    }
  }

  val counter = actorSystem.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print


  val account = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val person = actorSystem.actorOf(Props[Person], "aBillionaire")

  person ! LiveTheLife(account)

}