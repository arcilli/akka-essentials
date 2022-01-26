package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.Counter.Increment
import part2actors.CounterActorNoMutableState.Counter.{Decrement, Print}

object CounterActorNoMutableState extends App {
  val system = ActorSystem("counterActorNoMutableState")

  /**
   * Exercises
   * 1 - recreate the Counter Actor & no mutable state
   */

  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {
    override def receive: Receive = countReceive(0)

    // "Receive" type is actually Actor.Receive, which is actually PartialFunction[Any, Unit]
    def countReceive(currentCount: Int): PartialFunction[Any, Unit] = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my counter is $currentCount")
    }
  }

  val counter: ActorRef = system.actorOf(Props[Counter], "myCounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print
}
