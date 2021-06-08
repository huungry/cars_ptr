package com.hungry.cars.services

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.domain.error.CarsError.{CarAlreadyExists, CarNotFound}
import com.hungry.cars.domain.{Car, CarId}
import com.hungry.cars.http.in.{CreateCarRequest, UpdateCarRequest}

class CarsService(carsRepository: CarsRepository) {

  def carsFromBrand(brand: String): IO[List[Car]] = {
    carsRepository.findByBrand(brand)
  }

  def create(createCarRequest: CreateCarRequest): IO[Either[CarAlreadyExists, Unit]] = {
    type Err = CarAlreadyExists

    val CreateCarRequest(brand, model, _) = createCarRequest

    val effect: EitherT[IO, Err, Unit] = for {
      maybeCar <- EitherT.liftF(carsRepository.findByBrandAndModel(brand, model))
      _ <- EitherT.fromEither[IO](
             maybeCar
               .map(car => CarAlreadyExists(car.brand, car.model).asLeft)
               .getOrElse(().asRight)
           )
      car = createCarRequest.toCar
      _ <- EitherT.liftF(carsRepository.create(car))
    } yield ()

    effect.value
  }

  def update(carId: CarId, updateCarRequest: UpdateCarRequest): IO[Either[CarNotFound, Unit]] = {

    type Err = CarNotFound

    val effect: EitherT[IO, Err, Unit] = for {
      dbCar <- EitherT.fromOptionF(
                 carsRepository
                   .findCar(carId),
                 CarNotFound(carId)
               )
      car = updateCar(dbCar, updateCarRequest)
      _ <- EitherT.liftF(carsRepository.update(car))
    } yield ()

    effect.value
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

  def delete(carId: CarId): IO[Either[CarNotFound, Unit]] = {
    type Err = CarNotFound

    val effect: EitherT[IO, Err, Unit] = for {
      _ <- EitherT.fromOptionF(
             carsRepository
               .findCar(carId),
             CarNotFound(carId)
           )
      _ <- EitherT.liftF(carsRepository.markAsDeleted(carId))
    } yield ()
    effect.value
  }

}
