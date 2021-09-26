package part1.recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  val account = new BankAccount(2000)

  /**
   * 1. OOP incapsulation only valid in the SINGLE THREADED model
   */
  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = synchronized {
      this.amount -= money
    }

    def deposit(money: Int) = this.amount += synchronized {
      money
    }

    def getAmount() = amount
  }
  //  for (_ <- 1 to 1000) {
  //    new Thread(() => account.withdraw(1)).start()
  //  }
  //
  //  for (_ <- 1 to 1000) {
  //    new Thread(() => account.deposit(1)).start()
  //  }

  println(account.getAmount())

  // OOP encapsulation is broken in a multithreaded environment => synchronization! Locks to the rescue

  // deadlocks, livelocks

  /**
   * 2. Delegate a task to the background/send a signal to a thread
   * Delegating something to the thread is a thread
   */

  // you have a running thread & you want to pass a runnable to that thread

  var task: Runnable = null
  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task...")
        task.run()
        task = null
      }
    }
  })

  def delegate2BackgroundThread(r: Runnable) = {
    if (task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(500)
  delegate2BackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegate2BackgroundThread(() => println("This should run in the background"))

  /**
   * #3: tracing & dealing with errors in multithreaded env is a PITN
   */
  // 1M numbers in between 10 threads

  import scala.concurrent.ExecutionContext.Implicits.global
  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0-999999, 1000000-199999...
    .map(range => Future {
      if (range.contains(546735)) throw new RuntimeException("Invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ +_) // Future with the sum of all the numbers
  sumFuture.onComplete(println)
}
