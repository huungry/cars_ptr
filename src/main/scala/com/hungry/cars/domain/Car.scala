package com.hungry.cars.domain

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class Car(id: CarId, brand: String, model: String, price: Long)

object Car {
  implicit val carCodec: Codec[Car] = deriveCodec[Car]
}
