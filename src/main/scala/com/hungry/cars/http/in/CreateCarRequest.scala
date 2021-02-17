package com.hungry.cars.http.in

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CreateCarRequest(brand: String, model: String, price: Long)

object CreateCarRequest {
  implicit val createCarRequestCodec: Codec[CreateCarRequest] = deriveCodec[CreateCarRequest]
}
