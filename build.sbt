import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.skapadia",
  version := "0.1.0",
  scalaVersion := "2.11.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "spray-playground")

scalacOptions ++= Seq(
  "-Xlint",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xfatal-warnings",
  "-Ywarn-unused-import"
)

scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import"))

libraryDependencies ++= Seq(sprayDeps, akkaDeps, scalatestDeps, loggingDeps).flatten

resolvers ++= resolutionRepos

