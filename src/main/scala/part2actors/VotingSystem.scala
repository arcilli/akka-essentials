package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object VotingSystem extends App {
  val system = ActorSystem("votingSystemDemo")
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])
  val voteAggregator = system.actorOf(Props[VoteAggregator])

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class Vote(candidate: String)

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) => {
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens,
          // Here's the initialization I've used to use above.
          Map()
        ))
      }
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        // a citizen hasn't voted yet

        // send another request, maybe this time he'll respond
        sender() ! VoteStatusRequest // ofc this might end up in an infinite loop

      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newStats")
        } else {
          // I still need to process some statuses
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  /**
   * Exercise  2 - simplified voting system
   */

  case object VoteStatusRequest

  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
    Print the status of the votes

    Martin - 1
    Jonas - 1
    Roland - 2
   */
}
