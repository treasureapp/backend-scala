package org.fantastic.AkkaPoc

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpRequest
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString

import language.postfixOps
import scala.concurrent.duration._

/**
  * Created by gcrowell on 2017-06-04.
  */
// see http://doc.akka.io/docs/akka/current/scala/actors.html#value-classes-as-constructor-arguments
class HttpArgument(val symbol: String) extends AnyVal


class HttpRequestActor(val arg: HttpArgument) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  val httpRequest = HttpRequest(uri = "http://akka.io")

  val http = Http(context.system)
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def preStart() = {
    http.singleRequest(httpRequest).pipeTo(self)
  }

  def receive = {
    case message: String => println(s"${arg.symbol} received the message: ${message}.")
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + body.utf8String)
      }
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    case _ => println(s"${arg.symbol} received a message.")
  }
}

object HttpRequestActor {
  //  def props1(arg: HttpArgument) = Props(classOf[ValueClassActor], arg) // fails at runtime
  //  def props2(arg: HttpArgument) = Props(classOf[ValueClassActor], arg.symbol) // ok
  def props3(arg: HttpArgument): Props = Props(new HttpRequestActor(arg)) // ok
}


object HttpActorSystemRoot extends App {
  // application entry point
  override def main(args: Array[String]): Unit = {
    // create a new actor system
    val actorSystem = ActorSystem("actor_system")
    // create parameter for Actor
    val symbolArgument = new HttpArgument("MSFT")
    // create Props configuration needed to safely start an actor
    val props = HttpRequestActor.props3(symbolArgument)
    // create (and start) the actor
    val actorRef = actorSystem.actorOf(props, "an_actor_created_using_value_class")


    // import ExecutionContextExecutor
    import actorSystem.dispatcher
    // start actor system (no idea what scheduler is or does)
    actorSystem.scheduler.scheduleOnce(500 millis) {
      // send message to actor in the system
      actorRef ! "test"
    }

  }
}