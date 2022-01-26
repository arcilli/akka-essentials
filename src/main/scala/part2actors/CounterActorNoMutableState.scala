package part2actors

import akka.actor.{Actor, ActorSystem}

object CounterActorNoMutableState extends App {
  val system = ActorSystem("conterActorNoMutableState")
  /**
   * Exercises
   * 1 - recreate the Counter Actor & no mutable state
   */

  class Counter extends Actor {
    override def receive: Receive = ???
  }
}
