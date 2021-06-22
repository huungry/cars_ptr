package com.hungry.cars.http.in

import com.hungry.cars.domain.User
import com.hungry.cars.domain.UserId
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.EncoderOps

case class CreateUserRequest(username: String, password: String, firstName: String, lastName: String, age: Int) {

  def toUser: User = User(
    id        = UserId.generate,
    username  = username,
    password  = password,
    firstName = firstName,
    lastName  = lastName,
    age       = age
  )

  private def toJson(createUserRequest: CreateUserRequest): Json = {
    Map(
      "username"  -> createUserRequest.username,
      "password"  -> createUserRequest.password,
      "firstName" -> createUserRequest.firstName,
      "lastname"  -> createUserRequest.lastName,
      "age"       -> createUserRequest.age.toString
    ).asJson
  }

  implicit val createUserRequestEncoder: Encoder[CreateUserRequest] = (createUserRequest: CreateUserRequest) =>
    toJson(createUserRequest)

}
