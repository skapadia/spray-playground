package com.skapadia.spray.playground.examples

import scala.concurrent._
import scala.util.{ Failure, Success }

import spray.httpx.SprayJsonSupport
import spray.json._
import spray.util.{ LoggingContext, SprayActorLogging }

import spray.routing._

import com.skapadia.spray.playground.hal._

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
case class Transaction(id: Long, amount: Double)
case class Account(id: Long, primary: Person, transactions: Seq[Transaction])
case class AccountId(id: Long)

object DomainMarshalling extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat3(Person)
  implicit val transactionFormat = jsonFormat2(Transaction)
  implicit val accountFormat = jsonFormat3(Account)
  implicit val accountIdFormat = jsonFormat1(AccountId)
}

import HALable._
import DomainMarshalling._
import SprayJsonSupport._

trait ExampleApi {

  implicit protected def executionContext: ExecutionContext

  protected def contextPath: String

  protected def getAccount(id: Long): Future[Option[Account]]

  protected def accountToHAL(account: Account): HALResponse[AccountId] = {
    val halPerson = HALResponse(account.primary)
    val halTransactions = account.transactions map { HALResponse(_) }
    //val halTransaction = HALResponse(account.transactions.head, Map())
    HALResponse(AccountId(account.id), Some(Map("person" -> List(halPerson), "transactions" -> halTransactions)))
  }

}

trait ExampleApiRoutes extends HttpServiceBase with ExampleApi with ExampleOperations {

  val routes = {
    pathPrefix("accounts") {
      path(LongNumber) { id =>
        pathEndOrSingleSlash {
          get { 
            complete { 
              getAccount(id) map { _.map { accountToHAL(_) } } 
            } 
          }
        }
      }
    }
  }
}

trait ExampleOperations {

  implicit protected def executionContext: ExecutionContext

  protected def getAccount(id: Long): Future[Option[Account]] = {
    Future.successful(Some(Account(id, Person(1, "Joe", "Schmoe"), List(Transaction(1, 100.0D)))))
  }

}

