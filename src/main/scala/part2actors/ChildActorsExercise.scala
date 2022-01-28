package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorsExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

object ChildActorsExercise extends App {
  // Distributed Word counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)

    // I can get the child index by checking the number of child
    case class WordCountTask(id: Int, text: String)

    // the <<<id>>> from the reply would be the equal to the id of the task
    // you need to know which reply is corresponding to
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {
    override def receive: Receive = {
      case Initialize(noChildren) =>
        println("[master] initializing...")
        val childrenRefs = for (i <- (1 to noChildren)) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: ${text} - I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))

      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
      // problem: who is the original requester of the text?
      // it's not sender(), but the requester of the wordcount
      // -> i need to track the original Requester
    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, value) => {
        println(s"${self.path} I have received task $id with $value")
        sender() ! WordCountReply(id, value.split(" ").length)
      }
    }
  }

  class TestActor extends Actor {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love akka", "scala is supeeer dooope dope dope", "yes", "me 2")
        texts.foreach(text => master ! text)

      case count: Int =>
        println(s"s[tesst actor] I received a reply: ${count}")
    }
  }

  val system = ActorSystem("roundRobinCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")

  testActor ! "go"

  /*
  Flow:
  create WordCounterMaster
  send Initialize(10) to WordCounterMaster
    -> 10 WordCounterWorkers will be created
  send "Akka is awesome" to WordCounterMaster
    wordCounterMaster will send a WordCounterTask("...") to one of its children
      child replies with a WordCountReply(3) to the master
    master replies with 3 to the sender

    requester -> wcm -> wcw
            r <- wcm <-
   */

  // Round Robin logic
}
