package com.hungry.cars.http.in

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.EncoderOps

final case class RegistrationData(username: String, password: String, firstName: String, lastName: String, age: Int) {

  private def toJson(registrationData: RegistrationData): Json = {
    Map(
      "username"  -> registrationData.username,
      "firstName" -> registrationData.firstName,
      "lastname"  -> registrationData.lastName,
      "age"       -> registrationData.age.toString
    ).asJson
  }

  implicit val registrationDataEncoder: Encoder[RegistrationData] = (registrationData: RegistrationData) =>
    toJson(registrationData)
}
