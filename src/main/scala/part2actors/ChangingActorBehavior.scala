package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.{Ask, CHOCOLATE, Food, MomStart, VEGETABLE}

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {

    import FussyKid._


    // internal state of the kid
    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (HAPPY == state) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
      case Food(CHOCOLATE) => context.unbecome() // revert to previous behavior
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidReference: ActorRef)

    case class Food(food: String)

    case class Ask(message: String) // do you want to play?

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {

    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy.")
      case KidReject => println("My kid is sad, but he's healthy.")
    }
  }

  val system = ActorSystem("changingActorBehaviorDemo")

  val fussyKid = system.actorOf(Props[FussyKid], "fussyKid")
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])

  val mom = system.actorOf(Props[Mom])

  mom ! MomStart(fussyKid)
  mom ! MomStart(statelessFussyKid)

  /*
  mom receives MomStart
    kid receives Food(VEGETABLE) -> kid will change the handler to sadReceive
    kid receives Ask(play??) -> kid replies with sadReceive handler
  mom receives KidReject
   */

  /*
    discardOld = TRUE or FALSE

    discardOld == unspecified (TRUE by default)
    Food(veg) -> message handler will bebe set to `sadReceive`
    Food(chocolate) -> handler become `happyReceive`


    context.become

    discardOld - false
    Food(veg) -> stack.push(sadReceive)
    Stack:
    1. sadReceive
    2. happyReceive

  Food(chocolate) -> stack.push(happyReceive)
   Stack:
   1. happyReceive
   2. sadReceive
   3. happyReceive
   */


  /*
    new behavior
    context.unbecome


    Food(veg)
    Food(veg)
    Food(chocolate)
    Food(chocolate)

    Stack:
    1. sadReceive
    2. sadReceive
    2. happyReceive
   */
}
