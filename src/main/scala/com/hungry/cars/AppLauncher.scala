package com.hungry.cars

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect._
import cats.syntax.semigroupk._
import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.db.repository.CarsRepositoryDoobie
import com.hungry.cars.db.repository.UserRepository
import com.hungry.cars.db.repository.UserRepositoryDoobie
import com.hungry.cars.http.routes.CarsRoutes
import com.hungry.cars.http.routes.UserRoutes
import com.hungry.cars.services.CarsService
import com.hungry.cars.services.UserService
import doobie._
import doobie.hikari._
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
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
      val carsRoutes: HttpRoutes[IO]     = new CarsRoutes(carsService).routes

      val userRepository: UserRepository = new UserRepositoryDoobie(xa)
      val userService: UserService       = new UserService(userRepository)
      val userRoutes: HttpRoutes[IO]     = new UserRoutes(userService).routes

      val allRoutes: HttpRoutes[IO] = {
        carsRoutes <+> userRoutes
      }

      val allRoutesCompleted: HttpApp[IO] = {
        allRoutes.orNotFound
      }

      server(allRoutesCompleted)
    }
  }

}
