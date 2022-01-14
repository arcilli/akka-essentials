package part2.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {
  // part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors

  // word count actor
  class WordCounterActor extends Actor {
    var totalWords = 0

    def receive: PartialFunction[Any, Unit] = {
      case message: String => {
        totalWords += message.split(" ").length
        println(s"[word counter] I have received <<${message}>>")
      }
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // partial3 - instantiate our actor
  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCounterActor], "wordCounter")
  val anotherWordCounter: ActorRef = actorSystem.actorOf(Props[WordCounterActor], "anotherWordCounter")

  // part 4 - communicate with the actor
  // this sending of the message is completely asynchronous
  wordCounter ! "I am learning smth"
  anotherWordCounter ! "A different message"

  // initialize an actor with parameters
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"[person actor] Hi, my name is $name")
      case _ =>
    }
  }

  // this is legal, but it's discouraged
  val person = actorSystem.actorOf(Props(new Person("Bob")))
  person ! "hi"


  // preferred way:
  object Person {
    def props(name: String) = Props(new Person(name))
  }
  val person2 = actorSystem.actorOf(Person.props("Developed Bob"))
  person2 ! "hi"
}
