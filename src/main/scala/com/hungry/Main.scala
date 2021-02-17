package com.hungry

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import cats.implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    IO(println("Hello metals!")) >>
      IO(ExitCode.Success)
  }
}
