package com.hungry.cars.domain

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class User(
  id: UserId,
  username: String,
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  age: Int
)

object User {
  implicit val userCodec: Codec[User] = deriveCodec[User]
}
