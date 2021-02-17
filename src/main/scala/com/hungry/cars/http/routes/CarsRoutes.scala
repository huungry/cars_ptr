package com.hungry.cars.http.routes

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import com.hungry.cars.http.in.{CreateCarRequest, UpdateCarRequest}
import com.hungry.cars.services.CarsService
import org.http4s.EntityDecoder
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._

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
          response <- Created(carsService.create(createCarRequest))
        } yield response

      case req @ PATCH -> Root / "cars"  =>
        for {
          updateCarRequest <- req.as[UpdateCarRequest]
          response <- Ok(carsService.update(updateCarRequest))
        } yield response

    }
    .orNotFound

}
