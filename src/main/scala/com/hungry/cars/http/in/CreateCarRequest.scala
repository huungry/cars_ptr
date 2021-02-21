package com.hungry.cars.http.in

import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CreateCarRequest(brand: String, model: String, price: Long) {

  def toCar: Car = Car(id = CarId.generate, brand = brand, model = model, price = price)
}

object CreateCarRequest {
  implicit val createCarRequestCodec: Codec[CreateCarRequest] = deriveCodec[CreateCarRequest]
}
