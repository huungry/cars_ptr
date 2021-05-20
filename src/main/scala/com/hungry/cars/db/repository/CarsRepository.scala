package com.hungry.cars.db.repository

import cats.effect.IO
import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import doobie.Transactor
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant

trait CarsRepository {

  def doesCarExists(brand: String, model: String): IO[Boolean]

  def findByBrand(brand: String): IO[List[Car]]

  def findByBrandAndModel(brand: String, model: String): IO[Option[Car]]

  def findCar(carId: CarId): IO[Option[Car]]

  def getCar(carId: CarId): IO[List[Car]]

  def create(car: Car): IO[Unit]

  def update(car: Car): IO[Unit]

  def delete(carId: CarId): IO[Unit]
}

class CarsRepositoryDoobie(xa: Transactor[IO]) extends CarsRepository {

  def doesCarExists(brand: String, model: String): IO[Boolean] = {
    sql"""
      SELECT
      COUNT(*)
      FROM CARS
      WHERE BRAND = $brand AND MODEL = $model AND DELETED_AT IS NULL
      """
      .query[Int]
      .to[List]
      .transact(xa)
      .map(_.headOption.exists(_ > 0))
  }

  def findByBrand(brand: String): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE FROM CARS WHERE BRAND = $brand AND DELETED_AT IS NULL
    """
      .query[Car]
      .to[List]
      .transact(xa)
  }

  def findByBrandAndModel(brand: String, model: String): IO[Option[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE FROM CARS WHERE BRAND = $brand AND MODEL = $model AND DELETED_AT IS NULL
    """
      .query[Car]
      .option
      .transact(xa)
  }

  override def findCar(carId: CarId): IO[Option[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE FROM CARS WHERE ID = ${carId.value} AND DELETED_AT IS NULL
    """
      .query[Car]
      .option
      .transact(xa)
  }

  override def getCar(carId: CarId): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE FROM CARS WHERE ID = ${carId.value} AND DELETED_AT IS NULL
    """
      .query[Car]
      .to[List]
      .transact(xa)
  }

  override def create(car: Car): IO[Unit] = {
    sql"""
      INSERT INTO CARS
      (ID, BRAND, MODEL, PRICE)
      VALUES
      (${car.id.value}, ${car.brand}, ${car.model}, ${car.price})
    """.update.run
      .transact(xa)
      .as(())
  }

  override def update(car: Car): IO[Unit] = {
    sql"""
      UPDATE CARS
      SET BRAND = ${car.brand}, MODEL = ${car.model}, PRICE = ${car.price}
      WHERE
      ID = ${car.id.value} 
    """.update.run
      .transact(xa)
      .as(())
  }

  override def delete(carId: CarId): IO[Unit] = {

    val instant = Instant.now()

    sql"""
           UPDATE CARS
           SET DELETED_AT = $instant
           WHERE
           ID = ${carId.value}
         """.update.run
      .transact(xa)
      .as(())
  }

}
