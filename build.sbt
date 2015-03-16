import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.skapadia",
  version := "0.1.0",
  scalaVersion := "2.11.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "spray-playground"
  )

libraryDependencies ++= Seq(sprayDeps, akkaDeps, scalatestDeps, loggingDeps).flatten

resolvers ++= resolutionRepos

