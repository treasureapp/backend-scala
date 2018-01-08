// see http://doc.akka.io/docs/akka/current/scala/actors.html#creating-actors

package org.fantastic.AkkaPoc

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.actor.{ ActorSystem, Actor, ActorRef, Props, PoisonPill }
import language.postfixOps
import scala.concurrent.duration._
/**
  * Created by gcrowell on 2017-06-04.
  */


class ActorRoot extends Actor {
  val log = Logging(context.system, this)

  //FO EHERW

  def receive = {
    case "test" => log.info("received test")
    case "tesSDCt" => log.info("received test")
    case _      => log.info("received unknown message")
  }
}

object PocMain extends App {
  override def main(args: Array[String]): Unit = {
//    super.main(args)

    // create a new actor system
    val actorSystem = ActorSystem("test_actor_system")
    // add an actor to the system
    val actorRoot = actorSystem.actorOf(Props[ActorRoot], "root")

    // import ExecutionContextExecutor
    import actorSystem.dispatcher
    // start actor system
    actorSystem.scheduler.scheduleOnce(500 millis) {
      // send message to actor in the system
      actorRoot ! "test"
      actorRoot ! "JLKADJF"
      actorRoot ! "test"
    }
//    actorSystem.terminate()
  }
}