package com.hungry.cars.http.in

import cats.effect.IO
import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import com.hungry.cars.domain.error.CarsError.CarNotFound
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UpdateCarRequest(brand: Option[String], model: Option[String], price: Option[Long]) {

  //def toCar(carId: CarId): Car =
  //Car(id = carId, brand = brand, model = model, price = price)

  def updateBrand(currentCar: Car, newBrand: Option[String]): Car = {
    newBrand match {
      case Some(updatedBrand) =>
        Car(id = currentCar.id, brand = updatedBrand, model = currentCar.model, price = currentCar.price)
      case None => currentCar
    }
  }

  def updateModel(currentCar: Car, newModel: Option[String]): Car = {
    newModel match {
      case Some(updatedModel) =>
        Car(id = currentCar.id, brand = currentCar.brand, model = updatedModel, price = currentCar.price)
      case None => currentCar
    }
  }

  def updatePrice(currentCar: Car, newPrice: Option[Long]): Car = {
    newPrice match {
      case Some(updatedPrice) =>
        Car(id = currentCar.id, brand = currentCar.brand, model = currentCar.model, price = updatedPrice)
      case None => currentCar
    }
  }

  def updateCar(requestedCarId: CarId, currentCar: Option[Car], carRequest: UpdateCarRequest): Car = {

    val dbCar: Car = currentCar.getOrElse(???)

    updatePrice(updateModel(updateBrand(dbCar, carRequest.brand), carRequest.model), carRequest.price)

  }

}

object UpdateCarRequest {
  implicit val updateCarRequestCodec: Codec[UpdateCarRequest] = deriveCodec[UpdateCarRequest]
}
