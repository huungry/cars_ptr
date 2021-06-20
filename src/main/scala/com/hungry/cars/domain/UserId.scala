package com.hungry.cars.domain

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

import java.util.UUID

case class UserId(value: String) extends AnyVal

object UserId {

  def generate: UserId = {
    val uuid = UUID.randomUUID().toString
    UserId(uuid)
  }

  implicit val userIdCodec: Codec[UserId] = deriveUnwrappedCodec[UserId]
}
