package com.softwaremill.testing

import java.util.UUID

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats.effect.{Blocker, ContextShift, IO}

import scala.concurrent.ExecutionContext

class Example1Test extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private var postgres: EmbeddedPostgres = _
  private var transactor: Transactor[IO] = _
  implicit private val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    postgres = EmbeddedPostgres.builder().start()
    transactor = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      postgres.getJdbcUrl("postgres", "postgres"),
      "postgres",
      "postgres",
      Blocker.liftExecutionContext(ExecutionContext.global)
    )
    sql"CREATE TABLE user_points (user_id uuid PRIMARY KEY, points int NOT NULL)".update.run
      .transact(transactor)
      .unsafeRunSync()
  }

  import Example1._

  it should "add 1 point" in {
    // given
    val userId = UUID.randomUUID()
    addUserWithPoints(userId, 15)

    // when
    new Points(DefaultDao).increase(userId).transact(transactor).unsafeRunSync()

    // then
    readUserPoints(userId) shouldBe 16
  }

  it should "add 3 points" in {
    // given
    val userId = UUID.randomUUID()
    addUserWithPoints(userId, 16)

    // when
    new Points(DefaultDao).increase(userId).transact(transactor).unsafeRunSync()

    // then
    readUserPoints(userId) shouldBe 19
  }

  private def addUserWithPoints(userId: UUID, points: Int): Unit = {
    sql"INSERT INTO user_points(user_id, points) VALUES ($userId, $points)".update.run.transact(transactor).unsafeRunSync()
  }

  private def readUserPoints(userId: UUID): Int = {
    sql"SELECT points FROM user_points WHERE user_id = $userId".query[Int].unique.transact(transactor).unsafeRunSync()
  }

  override protected def afterAll(): Unit = {
    postgres.close()
    super.afterAll()
  }
}
