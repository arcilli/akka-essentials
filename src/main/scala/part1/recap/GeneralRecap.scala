package part1.recap

import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false


  // expressions
  val aConditionedVal = if (aCondition) 42 else 22

  // code block expression
  // the result of a CodeBlock is the result of its last expression
  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  // types
  // Unit - denotes the type of an expression that has only side effects
  val theUnit: Unit = println("Hello Scala")
  val aDog: Animal = new Dog
  // method notations
  val aCroc = new Crocodile
  // anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar")
  }

  // functions
  def aFunction(x: Int) = x + 1

  // recursion - TAIL recursion
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, n * acc)

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  // OOP
  class Animal

  class Dog extends Animal

  aCroc.eat(aDog)
  aCroc eat aDog

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch")
  }

  aCarnivore eat aDog

  // generics
  abstract class MyList[+A]

  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int) // a LOT in akka-essentials

  // EXCEPTIONS
  val aPotentialFailure = try {
    throw new RuntimeException("I did nothing.") // Nothing, this expression does NOT return nothing.
  } catch {
    case e: Exception => "I caught an exception"
  } finally {
    // do some side effects
    println("some logs")
  }

  // Functional programming
  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42) // this will produce 43
  // equivalent to incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x+1
  // Int => Int === Function1[Int, Int]

  // FP Is all about working with functions as first-class
  List(1,2,3).map(incrementer)
  // .map = HOF

  // for comprehensions
  val pairs = for {
    num <- List(1,2,3,4)
    char <- List("a", "b", "c", "d")
  } yield num + "-" + char

  // the above for-comprehension translates to the following:
  // List(1,2,3,4).flatmap(number => List("a", "b", "c", "d").map(char => number + "-" + char))

  // Seq, Array, List, Vector, Map, Tuples, Set

  // "collections"
  // Options & Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "2nd"
    case _ => "unknown"
  }
  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi,my name is $n"
    case _ => s"I dont know my name."
  }
}
