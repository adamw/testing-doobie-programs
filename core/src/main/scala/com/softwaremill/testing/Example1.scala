package com.softwaremill.testing

object Example1 {
  import java.util.UUID

  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  class Points(dao: Dao) {
    def increase(userId: UUID): ConnectionIO[Unit] =
      for {
        current <- dao.currentPoints(userId)
        updated = calculatePointsIncrease(current)
        _ <- dao.updatePoints(userId, updated)
      } yield ()

    private def calculatePointsIncrease(points: Int): Int = {
      if (points % 2 == 0) points + 3 else points + 1
    }
  }

  trait Dao {
    def currentPoints(userId: UUID): ConnectionIO[Int]
    def updatePoints(userId: UUID, value: Int): ConnectionIO[Unit]
  }

  object DefaultDao extends Dao {
    override def currentPoints(userId: UUID): doobie.ConnectionIO[Int] =
      sql"SELECT points FROM user_points WHERE user_id = $userId".query[Int].unique

    override def updatePoints(userId: UUID, value: Int): doobie.ConnectionIO[Unit] =
      sql"UPDATE user_points SET points = $value WHERE user_id = $userId".update.run.map(_ => ())
  }
}
