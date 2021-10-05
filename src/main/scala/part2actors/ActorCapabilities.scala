package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.BankAccount.{Deposit, Statement, TransactionFailure, TransactionSuccess, Withdraw}
import part2actors.ActorCapabilities.Counter.{Decrement, Increment, Print}
import part2actors.ActorCapabilities.Person.LiveTheLife

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

  /**
   * Exercises
   *
   * 1. a Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   *
   * 2. create a bank account as an actor
   * receives
   *    - Deposit an amount
   *    - Withdraw an amount
   *    - Statement
   *
   * replies with:
   *    - Success/
   *    - Failure
   *      on the above operations
   *
   * interact with some other kind of actor
   */

  // The DOMAIN of the counter
  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter]: My current count: $count")
    }
  }

  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  object BankAccount {
    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object Statement

    case class TransactionSuccess(message: String)

    case class TransactionFailure(reason: String)
  }

  // bank account
  class BankAccount extends Actor {
    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0)
          sender() ! TransactionFailure("invalid deposit amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"successfully deposited $amount")
        }
      case Withdraw(amount) => {
        if (amount < 0) {
          sender() ! TransactionFailure("invalid withdraw amount")
        } else if (amount > funds) {
          sender() ! TransactionFailure("insufficient funds")
        } else {
          funds -= amount
          sender() ! TransactionSuccess(s"successfully withdraw $amount")
        }
      }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message =>
        println(message.toString)
    }
  }

  val account = system.actorOf(Props[BankAccount], "BankAcc")
  val person = system.actorOf(Props[Person], "FooBar")

  person ! LiveTheLife(account)
}
