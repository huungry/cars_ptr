package com.hungry.cars.http.in

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UpdateCarRequest(brand: String, model: String, price: Long)

object UpdateCarRequest {
  implicit val updateCarRequestCodec: Codec[UpdateCarRequest] = deriveCodec[UpdateCarRequest]
}