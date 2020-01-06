package com.softwaremill.testing

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import cats.effect.{Blocker, ContextShift, IO, Resource, Sync}
import doobie._
import doobie.implicits._
import doobie.util.transactor.{Strategy, Transactor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.implicits._

import scala.concurrent.ExecutionContext

class Example4Test extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private var transactor: Transactor[IO] = _
  implicit private val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    transactor = Transactor(
      (),
      (_: Unit) => Resource.pure(null),
      KleisliInterpreter[IO](Blocker.liftExecutionContext(ExecutionContext.global)).ConnectionInterpreter,
      Strategy.void
    )
  }

  import Example1._

  it should "add 1 point" in {
    // given
    val updatedPoints = new AtomicInteger(0)
    val userId = UUID.randomUUID()
    val stubDao = newStubDaoConstantPoints(15, updatedPoints)

    // when
    new Points(stubDao).increase(userId).transact(transactor).unsafeRunSync()

    // then
    updatedPoints.get() shouldBe 16
  }

  it should "add 3 points" in {
    // given
    val updatedPoints = new AtomicInteger(0)
    val userId = UUID.randomUUID()
    val stubDao = newStubDaoConstantPoints(16, updatedPoints)

    // when
    new Points(stubDao).increase(userId).transact(transactor).unsafeRunSync()

    // then
    updatedPoints.get() shouldBe 19
  }

  def newStubDaoConstantPoints(points: Int, updatedPoints: AtomicInteger): Dao = new Dao {
    override def currentPoints(userId: UUID): ConnectionIO[Int] = points.pure[ConnectionIO]
    override def updatePoints(userId: UUID, value: Int): ConnectionIO[Unit] = Sync[ConnectionIO].delay(updatedPoints.set(value))
  }
}
