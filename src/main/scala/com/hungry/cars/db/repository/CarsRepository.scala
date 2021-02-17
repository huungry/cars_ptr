package com.hungry.cars.db.repository

import cats.effect.IO
import com.hungry.cars.domain.Car
import com.hungry.cars.http.in.CreateCarRequest
import doobie.Transactor
import doobie.implicits._

trait CarsRepository {

  def doesCarExists(brand: String, model: String): Boolean

  def findByBrand(brand: String): IO[List[Car]]

  def findByBrandAndModel(brand: String, model: String): IO[List[Car]]

  def create(car: Car): IO[Unit]

  def update(car: Car): IO[Unit]
}

class CarsRepositoryDoobie(xa: Transactor[IO]) extends CarsRepository {

  def doesCarExists(brand: String, model: String): Boolean = {
      sql"""
      select
      exists(SELECT ID, BRAND, MODEL, PRICE)
      from CARS
      where brand = $brand AND model = $model;
      """
        .query[Boolean]
        .to[List]
        .transact(xa)
        .unsafeRunSync()
        .exists(identity)
    }

  def findByBrand(brand: String): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where brand = $brand;
    """
      .query[Car]
      .to[List]
      .transact(xa)
  }

  def findByBrandAndModel(brand: String, model: String): IO[List[Car]] = {
    sql"""
      SELECT ID, BRAND, MODEL, PRICE from CARS where brand = ${brand} AND model = ${model};
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
      (${car.id.value}, ${car.brand}, ${car.model}, ${car.price});
    """
      .update.run
      .transact(xa)
      .as(())
  }

  override def update(car: Car): IO[Unit] = {
    println("CarsRepository update here")
    println(car.id.value, car.brand, car.model, car.price)

    sql"""
      update cars
      set price = ${car.price}
      where
      brand = ${car.brand} and model = ${car.model};
    """
      .update.run
      .transact(xa)
      .as(())
  }

}