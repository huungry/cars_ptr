package com.hungry.cars.http.routes

import cats.effect.{ContextShift, IO, Timer}
import com.hungry.cars.domain.CarId
import com.hungry.cars.domain.error.CarsError.{CarAlreadyExists, _}
import com.hungry.cars.http.in.{CreateCarRequest, UpdateCarRequest}
import com.hungry.cars.services.CarsService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, HttpApp, HttpRoutes}

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

    }
    .orNotFound

}
