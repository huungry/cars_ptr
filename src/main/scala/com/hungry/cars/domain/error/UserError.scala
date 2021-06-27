package com.hungry.cars.domain.error

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.EncoderOps

sealed trait UserError {
  def code: String
  def errorMessage: String
}

object UserError {

  case class UserAlreadyExists(userName: String) extends UserError {
    def code: String         = "user-error-001"
    def errorMessage: String = s"Username $userName already exists!"
  }

  case class UsernameHasSpecialCharacters(userName: String) extends UserError {
    def code: String         = "user-error-002"
    def errorMessage: String = s"Username $userName cannot contain special characters."
  }

  case class EmailIsNotValid(email: String) extends UserError {
    def code: String         = "user-error-003"
    def errorMessage: String = "Email is not valid!"
  }

  case class EmailAlreadyExists(email: String) extends UserError {
    def code: String         = "user-error-004"
    def errorMessage: String = s"Email $email already exists!"
  }

  case object PasswordDoesNotMeetCriteria extends UserError {
    def code: String = "user-error-005"

    def errorMessage: String =
      "Password must be at least 10 characters long, including an uppercase and a lowercase letter, one number and one special character."
  }

  case object PasswordsAreNotIdentical extends UserError {
    def code: String         = "user-error-006"
    def errorMessage: String = "Passwords are not identical!"
  }

  case object PasswordCanNotEncrypt extends UserError {
    def code: String         = "user-error-007"
    def errorMessage: String = "Password can not encrypt!"
  }

  case class FirstNameHasSpecialCharacters(firstName: String) extends UserError {
    def code: String         = "user-error-008"
    def errorMessage: String = s"First name $firstName cannot contain spaces, numbers or special characters."
  }

  case class LastNameHasSpecialCharacters(lastName: String) extends UserError {
    def code: String         = "user-error-009"
    def errorMessage: String = s"Last name $lastName cannot contain spaces, numbers or special characters."
  }

  case object AgeIsInvalid extends UserError {
    def code: String         = "user-error-010"
    def errorMessage: String = "You must be aged 18 and not older than 75 to use our services."
  }

  private def toJson(userError: UserError): Json = {
    Map("code" -> userError.code, "errorMessage" -> userError.errorMessage).asJson
  }

  implicit val userErrorEncoder: Encoder[UserError] = (userError: UserError) => toJson(userError)

}
