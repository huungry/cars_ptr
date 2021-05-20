package com.hungry.cars.http.routes

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import com.hungry.cars.domain.CarId
import com.hungry.cars.domain.error.CarsError.CarAlreadyExists
import com.hungry.cars.domain.error.CarsError._
import com.hungry.cars.http.in.CreateCarRequest
import com.hungry.cars.http.in.UpdateCarRequest
import com.hungry.cars.services.CarsService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.EntityDecoder
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.headers.`Accept-Patch`

class CarsRoutes(carsService: CarsService)(implicit cs: ContextShift[IO], timer: Timer[IO]) {

  implicit private val createCarRequestDecoder: EntityDecoder[IO, CreateCarRequest] = jsonOf[IO, CreateCarRequest]
  implicit private val updateCarRequestDecoder: EntityDecoder[IO, UpdateCarRequest] = jsonOf[IO, UpdateCarRequest]

  val routes: HttpApp[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "cars" / brand =>
        Ok(carsService.carsFromBrand(brand))

      case req @ POST -> Root / "cars" =>
        for {
          createCarRequest <- req.as[CreateCarRequest]
          response <- carsService
                        .create(createCarRequest)
                        .flatMap { either: Either[CarAlreadyExists, Unit] =>
                          either match {
                            case Left(carAlreadyExists: CarAlreadyExists) => Conflict(carAlreadyExists)
                            case Right(_)                                 => Created()
                          }
                        }
        } yield response

      case req @ PATCH -> Root / "cars" / id =>
        val carId = CarId(id)
        for {
          updateCarRequest <- req.as[UpdateCarRequest]
          response <- carsService
                        .update(carId, updateCarRequest)
                        .flatMap { either: Either[CarNotFound, Unit] =>
                          either match {
                            case Left(carNotFound: CarNotFound) => NotFound(carNotFound)
                            case Right(_)                       => Ok()
                          }
                        }
        } yield response

      case DELETE -> Root / "cars" / id =>
        val carId = CarId(id)
        for {
          response <- carsService
                        .delete(carId)
                        .flatMap { either: Either[CarNotFound, Unit] =>
                          either match {
                            case Left(carNotFound: CarNotFound) => NotFound(carNotFound)
                            case Right(_)                       => Accepted()
                          }
                        }
        } yield response

    }
    .orNotFound

}
