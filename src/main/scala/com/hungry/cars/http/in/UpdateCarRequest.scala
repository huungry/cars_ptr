package com.hungry.cars.http.in

import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UpdateCarRequest(brand: String, model: String, price: Long) {

  def toCar(carId: CarId): Car = Car(id = carId, brand = brand, model = model, price = price)

}

object UpdateCarRequest {
  implicit val updateCarRequestCodec: Codec[UpdateCarRequest] = deriveCodec[UpdateCarRequest]
}