package com.hungry.cars.services

import cats.effect.IO
import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.domain.error.CarsError.{CarAlreadyExists, CarNotFound}
import com.hungry.cars.domain.{Car, CarId}
import com.hungry.cars.http.in.{CreateCarRequest, UpdateCarRequest}

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

  def update(id: String, updateCarRequest: UpdateCarRequest): IO[Unit] = {

    val requestedCarId = CarId(id)

    for {
      maybeCar <- carsRepository.findCar(requestedCarId)
      _        <- maybeCar.map(_ => IO.pure(())).getOrElse(IO.raiseError(CarNotFound(requestedCarId)))
      _        <- carsRepository.update(updateCarRequest.updateCar(requestedCarId, maybeCar, updateCarRequest))
    } yield ()

  }

}
