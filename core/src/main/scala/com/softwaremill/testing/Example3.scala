package com.softwaremill.testing

object Example3 {
  import java.util.UUID

  import cats._
  import cats.implicits._

  class Points[F[_]: Monad](dao: Dao[F]) {
    def increase(userId: UUID): F[Unit] =
      for {
        current <- dao.currentPoints(userId)
        updated = calculatePointsIncrease(current)
        _ <- dao.updatePoints(userId, updated)
      } yield ()

    private def calculatePointsIncrease(points: Int): Int = {
      if (points % 2 == 0) points + 3 else points + 1
    }
  }

  trait Dao[F[_]] {
    def currentPoints(userId: UUID): F[Int]
    def updatePoints(userId: UUID, value: Int): F[Unit]
  }

  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  object DefaultDao extends Dao[ConnectionIO] {
    override def currentPoints(userId: UUID): doobie.ConnectionIO[Int] =
      sql"SELECT points FROM user_points WHERE user_id = $userId".query[Int].unique

    override def updatePoints(userId: UUID, value: Int): doobie.ConnectionIO[Unit] =
      sql"UPDATE user_points SET points = $value WHERE user_id = $userId".update.run.map(_ => ())
  }
}
