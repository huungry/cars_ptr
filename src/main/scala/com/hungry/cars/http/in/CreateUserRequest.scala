package com.hungry.cars.http.in

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class CreateUserRequest(
  username: String,
  email: String,
  password: String,
  passwordRepeat: String,
  firstName: String,
  lastName: String,
  age: Int
)

object CreateUserRequest {
  implicit val createUserDecoder: Decoder[CreateUserRequest] = deriveDecoder[CreateUserRequest]
}
