package com.hungry.cars.domain

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

import java.util.UUID

case class UserId(value: String) extends AnyVal

object UserId {

  implicit val userIdCodec: Codec[UserId] = deriveUnwrappedCodec[UserId]
}
