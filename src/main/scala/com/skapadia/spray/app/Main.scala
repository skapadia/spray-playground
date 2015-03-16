package com.skapadia.spray.app

import java.net.InetSocketAddress

import scala.concurrent.duration._

import akka.actor.{ ActorLogging, ActorSystem, Actor, ActorRef, Props, Status, Terminated }
import akka.pattern.ask
import akka.util.Timeout
import akka.io.{ IO, Tcp }

import spray.can.Http

import com.skapadia.spray.examples.ExampleHttpServiceActor

object Main extends App {

  initializeApp()

  private def initializeApp(): Unit = {
    implicit val system = ActorSystem("spray-playground-actor-system")
    val serverPort = 8080
    val serverAddress = new InetSocketAddress("localhost", serverPort)
    val serviceActor = system.actorOf(Props(new ExampleHttpServiceActor()))
    val serverSupervisor = system.actorOf(Props(new ServerSupervisor(serverAddress, serviceActor)))
  }

  class ServerSupervisor(serverAddress: InetSocketAddress, serviceActor: ActorRef) extends Actor
    with ActorLogging {
    
    override def preStart: Unit = {
      super.preStart
      context.actorOf(Props(new ServerInitializer(serverAddress, serviceActor)), "ServerActivator")
    }

    def receive: Actor.Receive = {
      case _ => ()
    }
  }

  // Disclaimer:  Using basic pattern followed at CCAD for starting up Spray services
  class ServerInitializer(serverAddress: InetSocketAddress, serviceActor: ActorRef) extends Actor
    with ActorLogging {

    import ServerInitializer._

    private var listenerRefMaybe: Option[ActorRef] = None

    override def preStart: Unit = {
      super.preStart
      bindConnector(serviceActor)
    }

    override def postStop: Unit = {
      super.postStop
      listenerRefMaybe foreach { _ ! Http.Unbind }
    }

    def receive: Actor.Receive = {
      case BindResult(_: Http.Bound) =>
        log.info(s"Bound to ${sender} at ${serverAddress}")
        listenerRefMaybe = Some(sender)
        context.watch(sender)

      case BindResult(Status.Failure(t)) =>
        // Log error that bind failed
        log.error(s"Failed to bind to ${serverAddress}", t)

      case BindResult(other) =>
      // Log error that bind failed for unknown reason
        log.error(s"Failed to bind to ${serverAddress}: $other")

      case Terminated(ref) =>
        log.warning(s"Re-attempting to bind to ${serverAddress} due to unexpected HttpListener termination")
        if (listenerRefMaybe.fold(false) { _ == ref }) bindConnector(serviceActor)
    }

    private def bindConnector(serviceActor: ActorRef): Unit = {
      val bindRequest = Http.Bind(listener = serviceActor, endpoint = serverAddress, backlog = 100, options = Nil, settings = None)
      context.actorOf(Props(new BindResultForwarder(bindRequest, self)))
    }
  }

  object ServerInitializer {
    case class BindResult(response: Any)
    class BindResultForwarder(request: Http.Bind, responseTo: ActorRef) extends Actor {
      import context.system
      IO(Http) ! request
      context.setReceiveTimeout(1.minute)
      def receive: Actor.Receive = {
        case rsp =>
          responseTo forward BindResult(response = rsp)
          context stop self
      }
    }
  }
}
