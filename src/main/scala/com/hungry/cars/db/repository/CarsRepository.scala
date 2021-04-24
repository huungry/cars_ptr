package com.hungry.cars.db.repository

import cats.effect.IO
import com.hungry.cars.domain.{Car, CarId}
import com.hungry.cars.http.in.CreateCarRequest
import doobie.Transactor
import doobie.implicits._

trait CarsRepository {

  def doesCarExists(brand: String, model: String): IO[Boolean]

  def findByBrand(brand: String): IO[List[Car]]

  def findByBrandAndModel(brand: String, model: String): IO[Option[Car]]

  def findCar(carId: CarId): IO[Option[Car]]

  def getCar(carId: CarId): IO[List[Car]]

  def create(car: Car): IO[Unit]

  def update(car: Car): IO[Unit]
}

class CarsRepositoryDoobie(xa: Transactor[IO]) extends CarsRepository {

  def doesCarExists(brand: String, model: String): IO[Boolean] = {
    sql"""
      select
      count(*)
      from CARS
      where brand = $brand AND model = $model
      """
      .query[Int]
      .to[List]
      .transact(xa)
      .map(_.headOption.exists(_ > 0))
  }

  def findByBrand(brand: String): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where brand = $brand
    """
      .query[Car]
      .to[List]
      .transact(xa)
  }

  def findByBrandAndModel(brand: String, model: String): IO[Option[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where brand = $brand AND model = $model
    """
      .query[Car]
      .option
      .transact(xa)
  }

  override def findCar(carId: CarId): IO[Option[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where ID = ${carId.value}
    """
      .query[Car]
      .option
      .transact(xa)
  }

  override def getCar(carId: CarId): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where ID = ${carId.value}
    """
      .query[Car]
      .to[List]
      .transact(xa)
  }

  override def create(car: Car): IO[Unit] = {
    println(car.id.value, car.brand, car.model, car.price)

    sql"""
      insert into cars
      (ID, BRAND, MODEL, PRICE)
      values
      (${car.id.value}, ${car.brand}, ${car.model}, ${car.price})
    """.update.run
      .transact(xa)
      .as(())
  }

  override def update(car: Car): IO[Unit] = {
    println("CarsRepository update here")
    println(car.id.value, car.brand, car.model, car.price)

    sql"""
      update cars
      set brand = ${car.brand}, model = ${car.model}, price = ${car.price}
      where
      ID = ${car.id.value}
    """.update.run
      .transact(xa)
      .as(())
  }

}
