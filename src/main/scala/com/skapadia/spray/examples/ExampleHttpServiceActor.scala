package com.skapadia.spray.examples

import scala.concurrent._
import scala.util.{ Failure, Success }

import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.util.{ LoggingContext, SprayActorLogging }

import spray.routing._
import spray.routing.directives.BasicDirectives._

import akka.actor.{ Actor, ActorLogging, ActorSystem }

class ExampleHttpServiceActor extends HttpServiceActor
  with ExampleApiRoutes
  with ActorLogging {

  implicit val loggingContext = LoggingContext.fromAdapter(log)

  implicit override protected val executionContext = context.dispatcher

  override protected def contextPath = "example"

  def receive: Actor.Receive = runRoute {
    pathPrefix(contextPath) { routes }
  }
}

case class Person(id: Long, firstName: String, lastName: String)
case class Account(id: Long, primary: Person)

object DomainMarshalling extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val personFormat = jsonFormat3(Person)
  implicit val accountFormat = jsonFormat2(Account)
}

trait ExampleApi {

  implicit protected def executionContext: ExecutionContext

  protected def contextPath: String

  protected def getAccount(id: Long): Future[Option[Account]]

}

trait ExampleApiRoutes extends HttpServiceBase with ExampleApi with ExampleOperations {

  import DomainMarshalling._

  val routes = {
    pathPrefix("accounts") {
      path(LongNumber) { id =>
        pathEndOrSingleSlash {
          get { complete(getAccount(id)) }
        }
      }
    }
  }
}

trait ExampleOperations {

  implicit protected def executionContext: ExecutionContext

  protected def getAccount(id: Long): Future[Option[Account]] = {
    Future.successful(Some(Account(id, Person(1, "Joe", "Schmoe"))))
  }
}

