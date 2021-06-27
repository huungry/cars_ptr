package com.hungry.cars.domain

case class ValidatedCreateUserRequest(
  username: String,
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  age: Int
)
