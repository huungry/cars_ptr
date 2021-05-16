package com.hungry.cars.services

import com.hungry.cars.db.repository.{CarsRepository, CarsRepositoryDoobie}
import com.hungry.cars.domain.{Car, CarId}
import com.hungry.cars.helpers.DatabaseTest
import com.hungry.cars.http.in.{CreateCarRequest, UpdateCarRequest}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, EitherValues}

final class CarsServiceTest
    extends AnyFlatSpec
    with Matchers
    with DatabaseTest
    with BeforeAndAfterEach
    with EitherValues {

  private val carsRepository: CarsRepository = new CarsRepositoryDoobie(testTransactor)
  private val carsService: CarsService       = new CarsService(carsRepository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    cleanUp()
  }

  override def afterEach(): Unit = {
    cleanUp()
    super.afterEach()
  }

  private def cleanUp(): Unit = {
    deleteFromTable("CARS")
  }

  // CREATE TESTS

  private val fordBrand        = "ford"
  private val focusModel       = "focus"
  private val createCarRequest = CreateCarRequest(fordBrand, focusModel, 100000)

  it should "create car" in {
    carsService.create(createCarRequest).unsafeRunSync().value

    val models: List[String] = carsRepository.findByBrand(fordBrand).unsafeRunSync().map(_.model)
    models should contain only focusModel
  }

  it should "not create car if car already exists" in {
    carsService.create(createCarRequest).unsafeRunSync().value
    carsRepository.findByBrand(fordBrand).unsafeRunSync().length shouldBe 1

    carsService.create(createCarRequest).unsafeRunSync().left.value
  }

  // UPDATE TESTS

  private val fordUpdated                = Some("FordUpdated")
  private val focusUpdated               = None
  private val priceUpdated: Option[Long] = Some(999)
  private val updateCarRequest           = UpdateCarRequest(fordUpdated, focusUpdated, priceUpdated)

  it should "update car" in {

    carsService.create(createCarRequest).unsafeRunSync().value

    val dbBrand       = createCarRequest.brand
    val dbModel       = createCarRequest.model
    val dbPrice: Long = createCarRequest.price

    val id: CarId =
      carsRepository
        .findByBrandAndModel(fordBrand, focusModel)
        .unsafeRunSync()
        .map(_.id)
        .getOrElse(CarId("IdNotFound"))

    carsService.update(id, updateCarRequest).unsafeRunSync().value

    val requestedBrand       = updateCarRequest.brand.getOrElse(dbBrand)
    val requestedModel       = updateCarRequest.model.getOrElse(dbModel)
    val requestedPrice: Long = updateCarRequest.price.getOrElse(dbPrice)

    val updatedCar: Car = Car(id, requestedBrand, requestedModel, requestedPrice)

    carsRepository.findCar(id).unsafeRunSync().map(car => car).getOrElse("CarNotFound") shouldBe updatedCar
  }

}
