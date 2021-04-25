package com.hungry.cars.http.in

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UpdateCarRequest(brand: Option[String], model: Option[String], price: Option[Long]) {}

object UpdateCarRequest {
  implicit val updateCarRequestCodec: Codec[UpdateCarRequest] = deriveCodec[UpdateCarRequest]
}
