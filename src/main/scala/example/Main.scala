package example

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import scala.util.Failure
import java.util.concurrent.ForkJoinPool

object Main {
  def main(args: Array[String]) {
    defaultEC
    pattern1_UnlimitedQueue
    pattern2_LimitedQueue
    scala.util.Try(pattern3_LimitedQueueNarrow) match {
      case Failure(e) => println("Fail!" + e.getMessage)
      case _ => ???
    }
    pattern4_LimitedQueueNarrow
    scala.util.Try(pattern5_LimitedQueueNarrow) match{
      case Failure(e) => println("Fail!" + e.getMessage)
      case _ => ???
    }
    pattern6_ForkJoinPool
  }
  
  def defaultEC() {
    import scala.concurrent.ExecutionContext.Implicits.global
    println("Start default ExecutionContext -------------------------")
    task(30)
  }
  
  def pattern1_UnlimitedQueue() {
    val pool = new ThreadPoolExecutor(1, 30, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable])
    implicit val ec = ExecutionContext.fromExecutorService(pool)
    println("Start pattern 1 use unlimited queue --------------------")
    callTask
  }

  def pattern2_LimitedQueue() {
    val pool = new ThreadPoolExecutor(1, 30, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable](3))
    implicit val ec = ExecutionContext.fromExecutorService(pool)
    println("Start pattern 2 use limited queue --------------------")
    callTask
  }

  def pattern3_LimitedQueueNarrow() {
    val pool = new ThreadPoolExecutor(1, 3, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable](3))
    implicit val ec = ExecutionContext.fromExecutorService(pool)
    println("Start pattern 3 use limited queue max 3 ---------------")
    callTask
  } //ThreadPoolExecutor.CallerRunsPolicy

  def pattern4_LimitedQueueNarrow() {
    val pool = new ThreadPoolExecutor(1, 3, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable](3), new ThreadPoolExecutor.CallerRunsPolicy)
    implicit val ec = ExecutionContext.fromExecutorService(pool)
    println("Start pattern 4 CallerRunsPolicy ---------------")
    callTask
  }

  def pattern5_LimitedQueueNarrow() {
    val pool = new ThreadPoolExecutor(1, 3, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable](3), new ThreadPoolExecutor.DiscardPolicy)
    implicit val ec = ExecutionContext.fromExecutorService(pool)
    println("Start pattern 5 DiscardPolicy ---------------")
    callTask
  }

  def pattern6_ForkJoinPool() {
    implicit val ec = ExecutionContext.fromExecutorService(new ForkJoinPool(30))
    println("Start pattern 6 ForkJoinPool ---------------")
    callTask
  }

  def callTask(implicit ec: ExecutionContextExecutorService) = try {
     task(30)
    } finally {
      ec.shutdown()
    }
  
  def task(x: Int)(implicit ec: ExecutionContext) = {
    Range(0, x).map{i=>
      val s = s"Future no.$i "
      println("Put: " + s)
      Future({proc(s, 1, 1000)})
    }.foreach{f => println("Result: " + Await.result(f, 10.seconds))}
    println("Finish!!!")
  }
  
  def proc(str: String, x:Int, sleep: Long): String = {
    Range(0,x).foreach { i => 
      Thread.sleep(sleep)
      println(str + i)
    }
    str
  }
}