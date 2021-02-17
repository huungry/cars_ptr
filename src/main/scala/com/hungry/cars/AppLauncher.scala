package com.hungry.cars

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect._
import com.hungry.cars.db.repository.{CarsRepository, CarsRepositoryDoobie}
import com.hungry.cars.http.routes.CarsRoutes
import com.hungry.cars.services.CarsService
import com.hungry.cars.db.repository.CarsRepositoryDoobie
import doobie._
import doobie.hikari._
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global

object AppLauncher extends IOApp {

  private def server(httpApp: HttpApp[IO]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  private def transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](4) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
              "org.postgresql.Driver", // driver classname
              "jdbc:postgresql:cars", // connect URL
              "kamil", // username
              "password", // password
              ce, // await connection here
              be // execute JDBC operations here
            )
    } yield xa

  override def run(args: List[String]): IO[ExitCode] = {
    transactor.use { xa: HikariTransactor[IO] =>
      val carsRepository: CarsRepository = new CarsRepositoryDoobie(xa)
      val carsService: CarsService       = new CarsService(carsRepository)
      val carsRoutes: HttpApp[IO]        = new CarsRoutes(carsService).routes
      server(carsRoutes)
    }
  }

}
