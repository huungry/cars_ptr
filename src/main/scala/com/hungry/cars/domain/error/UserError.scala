package com.hungry.cars.domain.error

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.EncoderOps

sealed trait UserError {
  def errorMessage: String
}

object UserError {

  case object UsernameHasSpecialCharacters extends UserError {
    def errorMessage: String = "Username cannot contain special characters."
  }

  case object PasswordDoesNotMeetCriteria extends UserError {

    def errorMessage: String =
      "Password must be at least 10 characters long, including an uppercase and a lowercase letter, one number and one special character."
  }

  case object FirstNameHasSpecialCharacters extends UserError {
    def errorMessage: String = "First name cannot contain spaces, numbers or special characters."
  }

  case object LastNameHasSpecialCharacters extends UserError {
    def errorMessage: String = "Last name cannot contain spaces, numbers or special characters."
  }

  case object AgeIsInvalid extends UserError {
    def errorMessage: String = "You must be aged 18 and not older than 75 to use our services."
  }

  case object UserAlreadyExists extends UserError {
    def errorMessage: String = "Username already exists!"
  }

  private def toJson(userRegError: UserError): Json = {
    Map("errorMessage" -> userRegError.errorMessage).asJson
  }

  implicit val userRegErrorEncoder: Encoder[UserError] = (userError: UserError) => toJson(userError)

}
