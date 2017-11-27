package org.fantastic.AkkaPoc

import akka.actor.{Actor, ActorSystem, Props}

import language.postfixOps
import scala.concurrent.duration._

/**
  * Created by gcrowell on 2017-06-04.
  */
// see http://doc.akka.io/docs/akka/current/scala/actors.html#value-classes-as-constructor-arguments
class Argument(val symbol: String) extends AnyVal


class ValueClassActor(val arg: Argument) extends Actor {
  def receive = {
    case message: String => println(s"${arg.symbol} received the message: ${message}.")
    case _ => println(s"${arg.symbol} received a message.")
  }
}

object ValueClassActor {
  //  def props1(arg: Argument) = Props(classOf[ValueClassActor], arg) // fails at runtime
  //  def props2(arg: Argument) = Props(classOf[ValueClassActor], arg.symbol) // ok
  def props3(arg: Argument): Props = Props(new ValueClassActor(arg)) // ok
}


object ActorSystemRoot extends App {
  // application entry point
  override def main(args: Array[String]): Unit = {
    // create a new actor system
    val actorSystem = ActorSystem("actor_system")
    // create parameter for Actor
    val symbolArgument = new Argument("MSFT")
    // create Props configuration needed to safely start an actor
    val props = ValueClassActor.props3(symbolArgument)
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