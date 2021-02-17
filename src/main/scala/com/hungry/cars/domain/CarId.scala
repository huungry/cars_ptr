package com.hungry.cars.domain

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

import java.util.UUID

case class CarId(value: String) extends AnyVal

object CarId {

  def generate: CarId = {
    val uuid = UUID.randomUUID().toString
    CarId(uuid)
  }

  implicit val carIdCodec: Codec[CarId] = deriveUnwrappedCodec[CarId]
}
