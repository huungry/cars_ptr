package com.hungry.cars.db.repository

import cats.effect.IO
import com.hungry.cars.domain.User
import doobie.Transactor
import doobie.implicits._
import doobie.implicits.legacy.instant._

trait UserRepository {
  def findByUsername(username: String): IO[Option[User]]
  def findByEmail(email: String): IO[Option[User]]
  def create(user: User): IO[Unit]
}

class UserRepositoryDoobie(xa: Transactor[IO]) extends UserRepository {

  override def findByUsername(username: String): IO[Option[User]] = {
    sql"""
      SELECT
      ID, USERNAME, EMAIL, PASSWORD, FIRSTNAME, LASTNAME, AGE
      FROM USERS
      WHERE USERNAME = $username
      """
      .query[User]
      .option
      .transact(xa)
  }

  override def findByEmail(email: String): IO[Option[User]] = {
    sql"""
      SELECT
      ID, USERNAME, EMAIL, PASSWORD, FIRSTNAME, LASTNAME, AGE
      FROM USERS
      WHERE EMAIL = $email
      """
      .query[User]
      .option
      .transact(xa)
  }

  override def create(user: User): IO[Unit] = {
    sql"""
      INSERT INTO USERS
      (ID, USERNAME, EMAIL, PASSWORD, FIRSTNAME, LASTNAME, AGE)
      VALUES
      (${user.id.value}, ${user.username}, ${user.email}, ${user.password}, ${user.firstName}, ${user.lastName}, ${user.age})
    """.update.run
      .transact(xa)
      .void
  }

}
