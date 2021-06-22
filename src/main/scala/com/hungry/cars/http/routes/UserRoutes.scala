package com.hungry.cars.http.routes

import cats.data._
import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import com.hungry.cars.domain.error.UserError
import com.hungry.cars.domain.error.UserError._
import com.hungry.cars.http.in.CreateUserRequest
import com.hungry.cars.services.UserService
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._

class UserRoutes(userService: UserService)(implicit cs: ContextShift[IO], timer: Timer[IO]) {

  implicit private val createUserRequestCodec: Codec[CreateUserRequest]               = deriveCodec[CreateUserRequest]
  implicit private val createUserRequestDecoder: EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]

  val routes: HttpRoutes[IO] = HttpRoutes
    .of[IO] {

      case req @ POST -> Root / "users" =>
        for {
          createUserRequest <- req.as[CreateUserRequest]
          response <- userService.UserRegService.validateForm(createUserRequest).flatMap {
                        either: Either[NonEmptyChain[UserError], CreateUserRequest] =>
                          either match {
                            case Left(userError: NonEmptyChain[UserError])   => Conflict(userError)
                            case Right(createUserRequest: CreateUserRequest) => Ok(createUserRequest)
                          }
                      }
        } yield response
    }
}
