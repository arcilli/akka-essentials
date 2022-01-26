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

    var candidate: Option[String] = None

    override def receive: Receive = {
      case Vote(c) =>  candidate = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  case class Vote(candidate: String)
  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {

    var stillWaiting: Set[ActorRef] = Set()
    var currentStats: Map[String, Int] = Map()

    override def receive: Receive = {
      case AggregateVotes(citizens) => {
        println("Let's aggregate votes")
        stillWaiting = citizens
        citizens.foreach(_ ! VoteStatusRequest)
      }
      case VoteStatusReply(None) =>
        // a citizen hasn't voted yet

        // send another request, maybe this time he'll respond
        sender() ! VoteStatusRequest // ofc this might end up in an infinite loop

      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        currentStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $currentStats")
        } else {
          stillWaiting = newStillWaiting
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
