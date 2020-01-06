package com.softwaremill.testing

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import cats.Id
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Example3Test extends AnyFlatSpec with Matchers {
  import Example3._

  it should "add 1 point" in {
    // given
    val updatedPoints = new AtomicInteger(0)
    val userId = UUID.randomUUID()
    val stubDao = newStubDaoConstantPoints(15, updatedPoints)

    // when
    new Points(stubDao).increase(userId)

    // then
    updatedPoints.get() shouldBe 16
  }

  it should "add 3 points" in {
    // given
    val updatedPoints = new AtomicInteger(0)
    val userId = UUID.randomUUID()
    val stubDao = newStubDaoConstantPoints(16, updatedPoints)

    // when
    new Points(stubDao).increase(userId)

    // then
    updatedPoints.get() shouldBe 19
  }

  def newStubDaoConstantPoints(points: Int, updatedPoints: AtomicInteger): Dao[Id] = new Dao[Id] {
    override def currentPoints(userId: UUID): Id[Int] = points
    override def updatePoints(userId: UUID, value: Int): Id[Unit] = updatedPoints.set(value)
  }
}
