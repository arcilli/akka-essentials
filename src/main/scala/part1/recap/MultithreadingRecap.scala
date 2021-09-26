package part1.recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {
  // creating threads on the JVM

  val aThread = new Thread(() => println("I'm running in parallel."))
  aThread.start()

  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("Hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("Goodbye")))
  threadHello.start()
  threadGoodbye.start()

  // different runs produce different results
  /**
   * ADDING @volatile only solves the "atomic read" part of the problem. Writes are still not atomic & you'll still
   * need "synchronized" for them.
   */
  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount

   def withdraw(money: Int) = this.amount -= money

    def safeWithdraw(money: Int) = this.synchronized{
      this.amount -= money
    }
  }
  /*
    BA (10000)
    T1 -> withdraw 1000
    T2 -> withdraw 2000

    this.amount = this.amount - 1000 is NOT ATOMIC
   */

  // inter-thread communication on the JVM
  // via the wait-notify mechanism

  // Scala Futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // doing a long computation - this will be evaluated on a different thread
    42
  }

  // callbacks
  future.onComplete{
    case Success(42) => println("Meaning of life")
    case Failure(exception) => println(exception)
  }

  // From a FP perspective, Future is a Monadic constructor => it has functional primitives
  val aProcessedFuture = future.map(_ + 1) // Future with value = 43
  val aFlatFuture = future.flatMap{
    value =>
      Future(value + 2)
  } // The result = a Future with value 44

  val filteredFuture = future.filter(_ %2 == 0) // NoSuchElementException

  // Future also support for comprehensions
  val aNonSenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield(meaningOfLife + filteredMeaning)

  // other utilities for Future(s)
  // andThen, recover/recoverWith

  // Promises

  // TODO: diff between Promises & Futures
}
