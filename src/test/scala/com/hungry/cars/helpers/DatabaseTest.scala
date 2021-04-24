package com.hungry.cars.helpers

import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.IO
import doobie.ExecutionContexts
import doobie.Fragment
import doobie.Transactor
import doobie.implicits._
import org.scalatest.TestSuite

trait DatabaseTest { this: TestSuite =>

  implicit private val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val testTransactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:cars", // connect URL
    "kamil", // username
    "password", // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  def deleteFromTable(tableName: String): Unit = {
    val deleteQuery = sql"DELETE FROM " ++ Fragment.const(tableName)
    deleteQuery.update.run.transact(testTransactor).void.unsafeRunSync()
  }

}
