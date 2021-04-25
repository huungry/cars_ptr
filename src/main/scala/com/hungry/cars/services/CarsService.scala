package com.hungry.cars.services

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.domain.Car
import com.hungry.cars.domain.error.CarsError.CarAlreadyExists
import com.hungry.cars.http.in.CreateCarRequest
import com.hungry.cars.http.in.UpdateCarRequest

class CarsService(carsRepository: CarsRepository) {

  def carsFromBrand(brand: String): IO[List[Car]] = {
    println(s"Searching cars from brand $brand")
    carsRepository.findByBrand(brand)
  }

  def create(createCarRequest: CreateCarRequest): IO[Either[CarAlreadyExists, Unit]] = {
    println(s"Got $createCarRequest to create new car")
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

//    carsRepository.findByBrandAndModel(brand, model).flatMap { maybeCar: Option[Car] =>
//      val either1: Either[CarAlreadyExists, Unit] =
//        maybeCar
//          .map(car => CarAlreadyExists(car.brand, car.model).asLeft)
//          .getOrElse(().asRight)
//
////      val either2: Either[CarAlreadyExists, Unit] =
////        Either
////          .fromOption(maybeCar, ())
////          .map(car => CarAlreadyExists(car.brand, car.model))
////          .swap
//
//      val car = createCarRequest.toCar
//      either1.fold(carAlreadyExists => IO.pure(carAlreadyExists.asLeft), _ => IO.pure(().asRight))
//    }

  }

//  def update(carId: CarId, updateCarRequest: UpdateCarRequest): IO[Unit] = {
//    for {
//      maybeCar <- carsRepository.findCar(carId)
//      car      <- maybeCar.map(car => IO.pure(car)).getOrElse(IO.raiseError(CarNotFound(carId)))
//      updatedCar = updateCar(car, updateCarRequest)
//      _ <- carsRepository.update(updatedCar)
//    } yield ()
//  }

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
