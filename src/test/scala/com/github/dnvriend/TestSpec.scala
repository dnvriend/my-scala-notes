package com.github.dnvriend

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.typelevel.scalatest.{DisjunctionMatchers, ValidationMatchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

abstract class TestSpec extends FlatSpec with Matchers with ValidationMatchers with DisjunctionMatchers with ScalaFutures with OptionValues {
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 60.minutes, interval = 300.millis)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit class PimpedFuture[T](self: Future[T]) {
    def toTry: Try[T] = Try(self.futureValue)
  }
}
