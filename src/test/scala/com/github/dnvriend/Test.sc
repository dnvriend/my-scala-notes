import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._

val result: Future[Int] = for {
  x <- Future(1)
  y <- Future(1 + x)
  z <- Future(1 + x + y)
} yield x + y + z

Await.result(result, 1.second)