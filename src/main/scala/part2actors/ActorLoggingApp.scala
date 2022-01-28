package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingApp extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    // #1 - explicit logging
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message =>
        // I want to log it.
        logger.info(message.toString)
    }
  }

  /**
   * Logging is generally done in 4 levels
   * 1 - DEBUG (the most verbose)
   * 2 - INFO
   * 3 - WARNING/WARN
   * 4 - ERROR (the most critical)
   */

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"

  // #2 - ActorLogging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (aV, bX) => log.info("Two `things`: {} and {}", aV, bX)
      case message =>
        log.info(message.toString)
    }
  }

  val simplerActor = system.actorOf(Props[ActorWithLogging])
  simplerActor ! "logging a simple message by extending a trait"
  simplerActor ! ("abc", 325)

  /**
   * LOGGING IS ASYNCHRONOUS!
   * Logging is implemented with actors itself.
   *
   */
}
