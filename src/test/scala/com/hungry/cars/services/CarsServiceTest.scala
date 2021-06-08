package com.hungry.cars.services

import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.db.repository.CarsRepositoryDoobie
import com.hungry.cars.domain.Car
import com.hungry.cars.domain.CarId
import com.hungry.cars.helpers.DatabaseTest
import com.hungry.cars.http.in.CreateCarRequest
import com.hungry.cars.http.in.UpdateCarRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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

  // GLOBAL VAL

  private val dbBrand          = "Ford"
  private val dbModel          = "Focus"
  private val dbPrice          = 99699L
  private val createCarRequest = CreateCarRequest(dbBrand, dbModel, dbPrice)

  // CREATE TESTS

  it should "create car" in {
    carsService.create(createCarRequest).unsafeRunSync().value

    val models: List[String] = carsRepository.findByBrand(dbBrand).unsafeRunSync().map(_.model)
    models should contain only dbModel
  }

  it should "not create car if car already exists" in {
    carsService.create(createCarRequest).unsafeRunSync().value
    carsRepository.findByBrand(dbBrand).unsafeRunSync().length shouldBe 1

    carsService.create(createCarRequest).unsafeRunSync().left.value
  }

  // UPDATE TESTS

  private val dbBrandUpdated   = Some("FordUpdated")
  private val dbModelUpdated   = None
  private val dbPriceUpdated   = Some(999L)
  private val updateCarRequest = UpdateCarRequest(dbBrandUpdated, dbModelUpdated, dbPriceUpdated)

  it should "update car" in {

    carsService.create(createCarRequest).unsafeRunSync().value

    val id: CarId =
      carsRepository
        .findByBrandAndModel(dbBrand, dbModel)
        .unsafeRunSync()
        .value
        .id

    carsService.update(id, updateCarRequest).unsafeRunSync().value

    val requestedBrand = updateCarRequest.brand.getOrElse(dbBrand)
    val requestedModel = updateCarRequest.model.getOrElse(dbModel)
    val requestedPrice = updateCarRequest.price.getOrElse(dbPrice)

    val updatedCar: Car = Car(id, requestedBrand, requestedModel, requestedPrice)

    carsRepository.findCar(id).unsafeRunSync().value shouldBe updatedCar
  }

  it should "not update car if ID is invalid" in {
    carsService.update(CarId("InvalidId"), updateCarRequest).unsafeRunSync().left.value
  }

  // DELETE TESTS

  it should "delete car" in {
    carsService.create(createCarRequest).unsafeRunSync().value

    val id: CarId =
      carsRepository
        .findByBrandAndModel(dbBrand, dbModel)
        .unsafeRunSync()
        .value
        .id

    carsService.delete(id).unsafeRunSync().value

    carsRepository.findCar(id).unsafeRunSync() shouldBe None
  }

}
