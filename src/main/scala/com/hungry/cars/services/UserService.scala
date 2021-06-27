package com.hungry.cars.services

import cats.data.NonEmptyChain
import cats.data.ValidatedNec
import cats.effect.IO
import cats.implicits.catsSyntaxTuple6Semigroupal
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import com.github.t3hnar.bcrypt._
import com.hungry.cars.db.repository.UserRepository
import com.hungry.cars.domain.User
import com.hungry.cars.domain.UserId
import com.hungry.cars.domain.ValidatedCreateUserRequest
import com.hungry.cars.domain.error.UserError
import com.hungry.cars.domain.error.UserError.AgeIsInvalid
import com.hungry.cars.domain.error.UserError.EmailAlreadyExists
import com.hungry.cars.domain.error.UserError.EmailIsNotValid
import com.hungry.cars.domain.error.UserError.FirstNameHasSpecialCharacters
import com.hungry.cars.domain.error.UserError.LastNameHasSpecialCharacters
import com.hungry.cars.domain.error.UserError.PasswordCanNotEncrypt
import com.hungry.cars.domain.error.UserError.PasswordDoesNotMeetCriteria
import com.hungry.cars.domain.error.UserError.PasswordsAreNotIdentical
import com.hungry.cars.domain.error.UserError.UserAlreadyExists
import com.hungry.cars.domain.error.UserError.UsernameHasSpecialCharacters
import com.hungry.cars.http.in.CreateUserRequest
import doobie.implicits.legacy.instant._

import java.util.UUID
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex

class UserService(userRepository: UserRepository) {

  private val specialCharactersRegex: Regex  = "^[a-zA-Z0-9]+$".r
  private val passwordCharactersRegex: Regex = "(?=^.{8,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$".r

  private val emailRegex: Regex =
    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".r

  private val minAge: Int = 18
  private val maxAge: Int = 75

  type ValidationResult[A] = ValidatedNec[UserError, A]

  private def validateUserNameIO(userName: String): IO[ValidationResult[String]] = {
    userName match {
      case specialCharactersRegex(_*) =>
        for {
          maybeUser: Option[User] <- userRepository.findByUsername(userName)
          res: ValidationResult[String] = maybeUser match {
                                            case Some(_) => UserAlreadyExists(userName).invalidNec
                                            case None    => userName.validNec
                                          }
        } yield res
      case _ =>
        IO {
          UsernameHasSpecialCharacters(userName).invalidNec
        }
    }
  }

  private def validateEmailIO(email: String): IO[ValidationResult[String]] = {
    email match {
      case emailRegex(_*) =>
        for {
          maybeUser: Option[User] <- userRepository.findByEmail(email)
          res: ValidationResult[String] = maybeUser match {
                                            case Some(_) => EmailAlreadyExists(email).invalidNec
                                            case None    => email.validNec
                                          }
        } yield res
      case _ =>
        IO {
          EmailIsNotValid(email).invalidNec
        }
    }
  }

  private def validatePassword(password: String, passwordRepeat: String): ValidationResult[String] =
    password match {
      case passwordCharactersRegex(_*) =>
        if (passwordRepeat == password) {
          validateEncryptedPassword(password, maybeEncryptPassword(password)) match {
            case Left(_)                  => PasswordCanNotEncrypt.invalidNec
            case Right(encryptedPassword) => encryptedPassword.validNec
          }
        } else PasswordsAreNotIdentical.invalidNec
      case _ => PasswordDoesNotMeetCriteria.invalidNec
    }

  private def maybeEncryptPassword(password: String): Try[String] = password.bcryptSafeBounded

  private def validateEncryptedPassword(
    password: String,
    maybeEncryptedPassword: Try[String]
  ): Either[UserError, String] = {
    maybeEncryptedPassword match {
      case Success(encryptedPassword) =>
        password.isBcryptedSafeBounded(encryptedPassword) match {
          case Success(true) => Right(encryptedPassword)
          case Failure(_)    => Left(UserError.PasswordCanNotEncrypt)
        }
      case Failure(_) => Left(UserError.PasswordCanNotEncrypt)
    }
  }

  private def validateFirstName(firstName: String): ValidationResult[String] =
    firstName match {
      case specialCharactersRegex(_*) => firstName.validNec
      case _                          => FirstNameHasSpecialCharacters(firstName).invalidNec
    }

  private def validateLastName(lastName: String): ValidationResult[String] =
    lastName match {
      case specialCharactersRegex(_*) => lastName.validNec
      case _                          => LastNameHasSpecialCharacters(lastName).invalidNec
    }

  private def validateAge(age: Int): ValidationResult[Int] =
    if (age >= minAge && age <= maxAge) age.validNec else AgeIsInvalid.invalidNec

  private def generateUserId: UserId = {
    val uuid = UUID.randomUUID().toString
    UserId(uuid)
  }

  private def toUser(userId: UserId, validatedCreateUserRequest: ValidatedCreateUserRequest): User = {
    val ValidatedCreateUserRequest(username, email, encryptedPassword, firstName, lastName, age) =
      validatedCreateUserRequest
    User(userId, username, email, encryptedPassword, firstName, lastName, age)
  }

  def createUser(
    createUserRequest: CreateUserRequest
  ): IO[Either[NonEmptyChain[UserError], ValidatedCreateUserRequest]] = {

    val CreateUserRequest(username, email, password, passwordRepeat, firstName, lastName, age) = createUserRequest

    for {
      validateUserName: ValidationResult[String] <- validateUserNameIO(username)
      validateEmail: ValidationResult[String]    <- validateEmailIO(email)
      x: Either[NonEmptyChain[UserError], ValidatedCreateUserRequest] =
        (
          validateUserName,
          validateEmail,
          validatePassword(password, passwordRepeat),
          validateFirstName(firstName),
          validateLastName(lastName),
          validateAge(age)
        ).mapN(ValidatedCreateUserRequest).toEither
      _ <- x match {
             case Right(validatedCreateUserRequest: ValidatedCreateUserRequest) =>
               userRepository.create(toUser(generateUserId, validatedCreateUserRequest))
             case Left(_) => IO.pure(())
           }
    } yield x
  }
}
