package com.treasure.data

/**
  * Created by gcrowell on 2017-06-23.
  */

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.{ByteString, Timeout}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
/**
  * Created by gcrowell on 2017-06-16.
  */
trait TextParser[A <: Record] {
  def parse(text: String): Seq[A]
  def toCSV(path: String): Unit = {
  }
}

trait DataDownloadRequest extends TextParser[Record] {

  def urlString: String

  def subject: Subject



}
/**
  * Root/Master
  * doesn't do anything.  just receives and forwards/routes DataDownloadRequest's to 1 of it's slaves.
  */
class DownloaderRootActor extends Actor with ActorLogging {

  val slavePoolSize = 5

  var router = {
    // create slave pool
    val slaves = Vector.fill(slavePoolSize) {
      val r = context.actorOf(Props[Downloader])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), slaves)
  }

  def receive = {
    case dataDownloadRequest: DataDownloadRequest =>
      // forward/route work to slave pool
      router.route(dataDownloadRequest, sender())
    case Terminated(a) =>
      // replace dead slaves
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[Downloader])
      context watch r
      router = router.addRoutee(r)
  }
}

/**
  * Many Downloader instances are started by Master
  *
  * receives DataDownloadRequest's from Master.
  * downloads (ie. executes single HttpRequest) the request
  * forwards downloaded data (eg. raw csv string data) to it's (only) child (ie. Parser)
  */
class Downloader extends Actor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  // create 1 Parser instance (aka "slave")
  val child = context.actorOf(Props[Parser], name = "parser")

  def receive = {

    case request: DataDownloadRequest => {
      log.info(s"${request.getClass} receieved")
      val httpRequest = HttpRequest(uri = request.urlString)
      val http = Http(context.system)
      val httpResponse = Await.result(http.singleRequest(httpRequest), Timeout(5 seconds).duration)
      child ! (httpResponse, request)
    }
    case _ => log.info(s"unhandled message received")
  }
}

/**
  * 1 Parser instance is started per Downloader instance
  *
  * receives HttpResponse from Downloader
  */
class Parser extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive: Receive = {
    case (HttpResponse(StatusCodes.OK, headers, entity, _), request: DataDownloadRequest) => {
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info(s"http response for ${request.subject.name} received.  parsing.")
        val htmlText = body.utf8String
        val structuredData = request.parse(htmlText)
        Spark.save(structuredData)
        log.info(structuredData.take(5).toString)
      }
    }
    case resp@HttpResponse(code, _, _, _) => {
      // TODO handle StatusCodes != OK and save log of error
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    }
    case _ => log.info("unhandled message received")
  }


}



object DemoDownloader extends App {

  override def main(args: Array[String]): Unit = {
    class Testing123 extends Actor with ActorLogging {

      private val masterRef = context.actorOf(Props[DownloaderRootActor], name = "master_consumer")

      override def receive: Receive = {
        case _ => {
          log.info("beginning test")
          masterRef ! new PriceDownloadRequest(Stock("MSFT"))
          masterRef ! new PriceDownloadRequest(Stock("DATA"))
          masterRef ! new PriceDownloadRequest(Stock("APPL"))
          masterRef ! new PriceDownloadRequest(Stock("FB"))
          masterRef ! new PriceDownloadRequest(Stock("GS"))
          masterRef ! new PriceDownloadRequest(Stock("NKE"))
        }
      }
    }
    val actorSystem = ActorSystem("actor_system")

    val testingRef = actorSystem.actorOf(Props[Testing123], "consuming_system")
    testingRef ! "request test"


    println("continue on doing other work while data is downloaded/parsed")
    Thread.sleep(10000)

    println("execution complete.  stopping actor system...")
    actorSystem.terminate()
  }
}
