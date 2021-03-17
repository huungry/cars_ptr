package com.hungry.cars.services

import cats.effect.IO
import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import com.hungry.cars.domain.error.CarsError.CarAlreadyExists
import com.hungry.cars.domain.error.CarsError.CarNotFound
import com.hungry.cars.http.in.CreateCarRequest
import com.hungry.cars.http.in.UpdateCarRequest

class CarsService(carsRepository: CarsRepository) {

  private def toCar(brand: String, model: String, price: Long): IO[Car] = {
    println(s"toCar: $brand, $model, $price")
    println(IO(Car(CarId.generate, brand, model, price)).unsafeRunSync())

    IO(Car(CarId.generate, brand, model, price))
  }

  def carsFromBrand(brand: String): IO[List[Car]] = {
    println(s"Searching cars from brand $brand")
    carsRepository.findByBrand(brand)
  }

  def create(createCarRequest: CreateCarRequest): IO[Unit] = {
    println(s"Got $createCarRequest to create new car")

    val CreateCarRequest(brand, model, _) = createCarRequest

    for {
      maybeCar <- carsRepository.findByBrandAndModel(brand, model)
      _        <- maybeCar.map(_ => IO.raiseError(CarAlreadyExists(brand, model))).getOrElse(IO.pure(()))
      _        <- carsRepository.create(createCarRequest.toCar)
    } yield ()
  }

  def update(carId: CarId, updateCarRequest: UpdateCarRequest): IO[Unit] = {
    for {
      maybeCar <- carsRepository.findCar(carId)
      car      <- maybeCar.map(car => IO.pure(car)).getOrElse(IO.raiseError(CarNotFound(carId)))
      updatedCar = updateCar(car, updateCarRequest)
      _ <- carsRepository.update(updatedCar)
    } yield ()
  }

  private def updateCar(car: Car, updateCarRequest: UpdateCarRequest): Car = {
    type UpdateCarOperation = Car => Option[Car]

    val updateBrand: UpdateCarOperation = (carToUpdate: Car) =>
      updateCarRequest.brand.map(brand => carToUpdate.copy(brand = brand))

    val updateModel: UpdateCarOperation = (carToUpdate: Car) =>
      updateCarRequest.model.map(model => carToUpdate.copy(model = model))

    val updatePrice: UpdateCarOperation = (carToUpdate: Car) =>
      updateCarRequest.price.map(price => carToUpdate.copy(price = price))

    val updates: List[UpdateCarOperation] = List(updateBrand, updateModel, updatePrice)

    updates.foldLeft(car) { (updatedCar: Car, update: UpdateCarOperation) =>
      update(updatedCar).getOrElse(updatedCar)
    }
  }

}
