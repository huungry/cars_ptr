package com.hungry.cars.db.repository

import cats.effect.IO
import com.hungry.cars.domain.User
import doobie.Transactor
import doobie.implicits._
import doobie.implicits.legacy.instant._

trait UserRepository {
  def doesUsernameExists(username: String): IO[List[User]]
}

class UserRepositoryDoobie(xa: Transactor[IO]) extends UserRepository {

  override def doesUsernameExists(username: String): IO[List[User]] = {
    println(s" USERNAME: $username repo here dosUsernameExists")
    sql"""
      SELECT
      ID, USERNAME, PASSWORD, FIRSTNAME, LASTNAME, AGE
      FROM USERS
      WHERE USERNAME = $username
      """
      .query[User]
      .to[List]
      .transact(xa)
  }

}
