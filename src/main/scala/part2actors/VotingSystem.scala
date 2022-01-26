package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.collection.mutable

object VotingSystem extends App {
  val system = ActorSystem("votingSystemDemo")
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])
  val voteAggregator = system.actorOf(Props[VoteAggregator])

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {

    var myOption: String = ""

    override def receive: Receive = {
      case Vote(value) => {
        println(s"I vote for $value.")
        myOption = value
      }
      // store with who I voted with
      case VoteStatusRequest =>
        println(s"Sent my vote for ${myOption}")
        sender() ! VoteStatusReply(Some(myOption))
    }
  }

  case class Vote(candidate: String)

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {

    val candidateAndNoVotes: mutable.Map[String, Int] = scala.collection.mutable.Map()

    override def receive: Receive = {
      case AggregateVotes(citizens) => {
        println("Let's aggregate votes")
        citizens.foreach(_ ! VoteStatusRequest)
      }
      case VoteStatusReply(reply) =>
        println("Received voteStatusReply")
        reply.map(candidateName => {
          val currentValue = candidateAndNoVotes.getOrElse(candidateName, 0)
          candidateAndNoVotes.put(candidateName, currentValue + 1)
        }
        )
        println(candidateAndNoVotes.map(pair => pair._1 + "=" + pair._2).mkString(","))
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
