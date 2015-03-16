import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "spray repo" at "http://repo.spray.io/",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  val sprayCan = "io.spray" %% "spray-can" % sprayV
  val sprayRouting = "io.spray" %% "spray-routing" % sprayV
  val sprayHttp = "io.spray" %% "spray-http" % sprayV
  val sprayHttpx = "io.spray" %% "spray-httpx" % sprayV
  val sprayJson = "io.spray" %% "spray-json" % "1.3.1"
  val sprayTestkit = "io.spray" %% "spray-testkit" % sprayV
  val sprayDeps = Seq(sprayCan, sprayRouting, sprayHttp, sprayHttpx, sprayJson, sprayTestkit % "test")

  val akkaDeps = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test")

  val scalatestDeps = Seq("org.scalatest" % "scalatest_2.11" % "2.2.1" % "test")

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.9"
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"
  val loggingDeps = Seq(logback)
}

