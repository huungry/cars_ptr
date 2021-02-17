package com.hungry.cars.services

import cats.effect.IO
import com.hungry.cars.db.repository.CarsRepository
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

    for {
      car <- if (carsRepository.doesCarExists(createCarRequest.brand, createCarRequest.model)) { IO.raiseError(new Exception("Already exists")) }
      else { toCar(createCarRequest.brand, createCarRequest.model, createCarRequest.price) }
      _ <- carsRepository.create(car)
    } yield ()
  }

  def update(updateCarRequest: UpdateCarRequest): IO[Unit] = {
    println(s"Got $updateCarRequest, new price: ${updateCarRequest.price} to update the car")

    for {
      car <- if (carsRepository.doesCarExists(updateCarRequest.brand, updateCarRequest.model)) { toCar(updateCarRequest.brand, updateCarRequest.model, updateCarRequest.price) }
      else { IO.raiseError(new Exception("There is no such car!")) }
      _ <- carsRepository.update(car)
    } yield()
  }

/*  private def errorIfCarExists(brand: String, model: String): IO[Unit] = {

    println(carsRepository.doesCarExists(brand, model).unsafeRunSync()) // logging

      for {
        exists <- carsRepository.doesCarExists(brand, model)
        _ <- if (exists.nonEmpty) IO.raiseError(new Exception("Already exists"))
        else IO.unit
      } yield ()
  }*/

}