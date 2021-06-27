package com.hungry.cars.http.routes

import cats.data._
import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import com.hungry.cars.domain.ValidatedCreateUserRequest
import com.hungry.cars.domain.error.UserError
import com.hungry.cars.domain.error.UserError.userErrorEncoder
import com.hungry.cars.http.in.CreateUserRequest
import com.hungry.cars.http.in.CreateUserRequest.createUserDecoder
import com.hungry.cars.services.UserService
import org.http4s.EntityDecoder
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._

class UserRoutes(userService: UserService)(implicit cs: ContextShift[IO], timer: Timer[IO]) {

  implicit private val createUserRequestDecoder: EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]

  val routes: HttpRoutes[IO] = HttpRoutes
    .of[IO] {

      case req @ POST -> Root / "users" =>
        for {
          createUserRequest <- req.as[CreateUserRequest]
          response <-
            userService.createUser(createUserRequest).flatMap {
              either: Either[NonEmptyChain[UserError], ValidatedCreateUserRequest] =>
                either match {
                  case Left(userError: NonEmptyChain[UserError]) => Conflict(userError)
                  case Right(_)                                  => Ok()
                }
            }
        } yield response
    }
}
