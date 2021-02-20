package com.hungry.cars.domain.error

import com.hungry.cars.domain.CarId
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._

import scala.util.control.NoStackTrace

sealed trait CarsError extends NoStackTrace {
  def code: String
  def message: String
}

object CarsError {

  case class CarAlreadyExists(brand: String, model: String) extends CarsError {
    override def code: String    = "cars-error-001"
    override def message: String = s"Car $brand, $model already exists."
  }

  case class CarNotFound(id: CarId) extends CarsError {
    override def code: String    = "cars-error-002"
    override def message: String = s"Car with id ${id.value} not found."
  }

  private def toJson(carsError: CarsError): Json = {
    Map("code" -> carsError.code, "message" -> carsError.message).asJson
  }

  implicit val carAlreadyExistsErrorEncoder: Encoder[CarAlreadyExists] = (carsError: CarAlreadyExists) =>
    toJson(carsError)
  implicit val carNotFoundErrorEncoder: Encoder[CarNotFound] = (carsError: CarNotFound) => toJson(carsError)

}
