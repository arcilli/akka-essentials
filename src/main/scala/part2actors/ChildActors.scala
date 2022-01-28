package part2actors

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import part2actors.ChildActors.Credit.{AttachToAccount, CheckStatus}
import part2actors.ChildActors.NaiveBankAccount.{Deposit, InitializeAccount, Withdraw}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  // Actors can create other actors

  object Parent {
    case class CreateChild(name: String)

    case class TellChild(message: String)
  }

  class Parent extends Actor {

    import Parent._


    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        // create a new actor inside the receive method
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got <<<<$message>>>>")
    }
  }

  val system = ActorSystem("parentChildDemo")

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("myChild")

  parent ! TellChild("heeei kid")

  // actor hierarchies
  // parent -> child -> grandChild
  //        -> child2 -> ....
  // ...

  /*
    Guardian actors (top-level)
    - /system = system guardian
    - /user = user-level guardian
    - / = the root guardian (manages both /system & /user guardian actors)


    you can check the "parent" of an actor by checking it's (hierarchical) path with self.path
   */

  /*
    find an actor by a path === Actor selection
   */
  val childSelection: ActorSelection = system.actorSelection("/user/parent/myChild")
  childSelection ! "I found you"

  val childSelection2: ActorSelection = system.actorSelection("/user/parent/myChild542151251")
  childSelection2 ! "I found you"

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE OR THE `THIS` REFERENCE TO CHILD ACTORS
   * NEVER EVER FOREVER.
   *
   * You'll break the encapsulation.
   */

  object NaiveBankAccount {
    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object InitializeAccount
  }

  class NaiveBankAccount extends Actor {
    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")

        // by passing here the "this" parameter, you're exposing
        creditCardRef ! AttachToAccount(this) // 'cause why not?

      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)
    }

    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on to of $amount")
      amount += funds
    }

    def withdraw(funds: Int) = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object Credit {

    // I should have as a parameter a generic ActorRef and not the actual implementation of NaiveBankAccount
    case class AttachToAccount(bankAccount: NaiveBankAccount) // !!

    case object CheckStatus
  }

  class CreditCard extends Actor {
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }

    def attachedTo(account: ChildActors.NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your message has been processed")
        // beningn coode

        account.withdraw(1) // because I can, ofc...
    }
  }

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)


  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // this is wrong


  // `Closing over` -> NEVER close over mutable state or `this`
}
