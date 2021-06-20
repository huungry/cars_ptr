package com.hungry.cars.services

import cats.data.NonEmptyChain
import cats.data.ValidatedNec
import cats.effect.IO
import cats.implicits.catsSyntaxTuple5Semigroupal
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.hungry.cars.db.repository.UserRepository
import com.hungry.cars.domain.User
import com.hungry.cars.domain.error.UserError
import com.hungry.cars.domain.error.UserError.AgeIsInvalid
import com.hungry.cars.domain.error.UserError.FirstNameHasSpecialCharacters
import com.hungry.cars.domain.error.UserError.LastNameHasSpecialCharacters
import com.hungry.cars.domain.error.UserError.PasswordDoesNotMeetCriteria
import com.hungry.cars.domain.error.UserError.UserAlreadyExists
import com.hungry.cars.domain.error.UserError.UsernameHasSpecialCharacters
import com.hungry.cars.http.in.CreateUserRequest
import doobie.implicits.legacy.instant._
import com.hungry.cars.http.in.RegistrationData

class UserService(userRepository: UserRepository) {

  sealed trait UserRegService {

    type ValidationResult[A] = ValidatedNec[UserError, A]

    private def validateUserNameIO(userName: String): IO[ValidationResult[String]] = {
      if (userName.matches("^[a-zA-Z0-9]+$")) {
        for {
          maybeUser: Option[User] <- userRepository.doesUsernameExists(userName).map(_.headOption)
          res: ValidationResult[String] = maybeUser match {
                                            case Some(_) => UserAlreadyExists.invalidNec
                                            case None    => userName.validNec
                                          }
        } yield res
      } else IO { UsernameHasSpecialCharacters.invalidNec }
    }

    private def validatePassword(password: String): ValidationResult[String] =
      if (password.matches("(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")) password.validNec
      else PasswordDoesNotMeetCriteria.invalidNec

    private def validateFirstName(firstName: String): ValidationResult[String] =
      if (firstName.matches("^[a-zA-Z]+$")) firstName.validNec else FirstNameHasSpecialCharacters.invalidNec

    private def validateLastName(lastName: String): ValidationResult[String] =
      if (lastName.matches("^[a-zA-Z]+$")) lastName.validNec else LastNameHasSpecialCharacters.invalidNec

    private def validateAge(age: Int): ValidationResult[Int] =
      if (age >= 18 && age <= 75) age.validNec else AgeIsInvalid.invalidNec

    def validateForm(createUserRequest: CreateUserRequest): IO[Either[NonEmptyChain[UserError], RegistrationData]] =
      for {
        validateUserName: ValidationResult[String] <- validateUserNameIO(createUserRequest.username)
        x: Either[NonEmptyChain[UserError], RegistrationData] = (
                                                                  validateUserName,
                                                                  validatePassword(createUserRequest.password),
                                                                  validateFirstName(createUserRequest.firstName),
                                                                  validateLastName(createUserRequest.lastName),
                                                                  validateAge(createUserRequest.age)
                                                                ).mapN(RegistrationData).toEither
      } yield x

  }
  object UserRegService extends UserRegService

}
