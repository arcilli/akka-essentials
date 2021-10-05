package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
  // part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  /*
    1. Actors are uniquely identified within an actorSystem.
    2. Messages are asynchronous.
    3. Each actor may respond differently.
    4. Actors are REALLY encapsulated.
   */

  // part 2 - create actors
  // word count actor

  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0

    // behavior
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word count] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[wordCounter] I cannot understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")


  // part 4 - communicate!
  // the sending of the message is asynchronous
  wordCounter ! "I'm learning Akka." // The "!" is also known as "tell"
  anotherWordCounter ! "Another message"
  //the same as
//  wordCounter.!("I'm learning akka.")

//  new WordCountActor - this will generate an exception at the runtime


  // Q: How do we define Actors with constructor arguments?

  class Person(name: String)
  new Person("Bobiță")

  class Person1(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  // This is legal, but discouraged.
  val person = actorSystem.actorOf(Props(new Person1("Ce_AiFăcutBobiță")))

  person ! "hi"

  // Fix -> create a companion object
  object Person1 {
    def props(name: String) = Props(new Person1(name))
  }
  val person1 = actorSystem.actorOf(Person1.props("Bobiță2"))
  person1 ! "hi"
}
