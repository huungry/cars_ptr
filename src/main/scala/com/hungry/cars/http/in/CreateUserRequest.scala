package com.hungry.cars.http.in

import com.hungry.cars.domain.User
import com.hungry.cars.domain.UserId
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CreateUserRequest(username: String, password: String, firstName: String, lastName: String, age: Int) {

  def toUser: User = User(
    id        = UserId.generate,
    username  = username,
    password  = password,
    firstName = firstName,
    lastName  = lastName,
    age       = age
  )
}

object CreateUserRequest {
  implicit val createUserRequestCodec: Codec[CreateUserRequest] = deriveCodec[CreateUserRequest]
}
