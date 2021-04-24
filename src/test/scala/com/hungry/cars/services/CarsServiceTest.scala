package com.hungry.cars.services

import com.hungry.cars.db.repository.CarsRepository
import com.hungry.cars.db.repository.CarsRepositoryDoobie
import com.hungry.cars.helpers.DatabaseTest
import com.hungry.cars.http.in.CreateCarRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues
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

}
