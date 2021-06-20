scalaVersion := "2.13.3"

name := "cars"
organization := "com.hungry.cars"
version := "1.0"

libraryDependencies ++= Seq(
  "org.typelevel"          %% "cats-core"                % "2.1.1",
  "org.typelevel"          %% "cats-effect"              % "2.3.1",
  "com.beachape"           %% "enumeratum-circe"         % "1.6.1",
  "org.latestbit"          %% "circe-tagged-adt-codec"   % "0.9.1",
  "io.circe"               %% "circe-core"               % "0.13.0",
  "io.circe"               %% "circe-generic"            % "0.13.0",
  "io.circe"               %% "circe-generic-extras"     % "0.13.0",
  "io.circe"               %% "circe-parser"             % "0.13.0",
  "org.tpolecat"           %% "doobie-core"              % "0.9.0",
  "org.tpolecat"           %% "doobie-hikari"            % "0.9.0",
  "org.tpolecat"           %% "doobie-postgres"          % "0.9.0",
  "org.http4s"             %% "http4s-circe"             % "0.21.18",
  "org.http4s"             %% "http4s-blaze-server"      % "0.21.18",
  "org.http4s"             %% "http4s-dsl"               % "0.21.18",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "ch.qos.logback"          % "logback-classic"          % "1.1.3" % Runtime,
  "org.postgresql"          % "postgresql"               % "9.3-1102-jdbc41",
  "org.scalatest"          %% "scalatest"                % "3.2.3",
  "org.reactormonk"        %% "cryptobits"               % "1.3"
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")

// lazy val root = (project in file(".")).
//   settings(
//     inThisBuild(List(
//       organization := "ch.epfl.scala",
//       scalaVersion := "2.13.3"
//     )),
//     name := "hello-world"
//   )
