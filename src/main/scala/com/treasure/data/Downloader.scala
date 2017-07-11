package com.treasure.data

/**
  * Created by gcrowell on 2017-06-23.
  */

import java.io.FileWriter

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by gcrowell on 2017-06-16.
  */


/**
  * parses raw text into into Seq[Record] (ie. a data table)
  *
  * @tparam A any type that implements the Record trait
  */
trait TextParser[A <: Record] {
  def parse(text: String): Seq[A]
}

/**
  * encapsulates all information required to download and parse into Seq[Record]
  */
trait DataDownloadRequest extends TextParser[Record] with Ticker {

  def urlString: String

  override def ticker: String

}

/**
  * Root/Master
  * doesn't do anything.  just receives and forwards/routes DataDownloadRequest's to 1 of it's slaves.
  */
class DownloaderRootActor extends Actor with ActorLogging {

  val slavePoolSize = 2

  var router = {
    // create slave pool
    val slaves = (0 to slavePoolSize - 1).map { (slaveIndex: Int) =>
      val r = context.actorOf(Props[Downloader], name = s"downloader-${slaveIndex}")
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
      log.info(s"handling request: ${request.getClass.getSimpleName} ${request.ticker} -> ${request.urlString}")
      implicit val ec = context.dispatcher
//      import ExecutionContext.Implicits.global
      val httpResponseFuture = Http(context.system).singleRequest(HttpRequest(uri = request.urlString))

      //      val httpResponse = Await.result(
      //        httpResponseFuture,
      //        Timeout(3 seconds).duration
      //      )
      //      log.info(s"request handled: ${request.getClass.getSimpleName} ${request.ticker}")
      //      child ! (httpResponseFuture, request)


      httpResponseFuture.onComplete {
        case Success(d) => child ! (d, request)
        case Failure(f) => log.info(s"http Failure: ${f.getMessage}")
        case _ =>
      }
//      child ! (httpResponseFuture.)
    }

    case _ => log.info(s"unhandled message received")
  }
}

/**
  * 1 Parser instance is started per Downloader instance
  *
  * receives tuple: (HttpResponse, DataDownloadRequest) from Downloader
  */
class Parser extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val child = context.actorOf(Props[Persister], name = "persist-data")

  override def receive: Receive = {
    case (HttpResponse(StatusCodes.OK, headers, entity, _), request: DataDownloadRequest) => {
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info(s"http response for ${request.ticker} received.  parsing.")
        //        val structuredData = request.parse(body.utf8String)
        val outFile = new FileWriter(s"${request.ticker}.txt")
        outFile.write(body.utf8String)
        outFile.close()
        //        child ! (structuredData, request)
        //        sender()
      }
    }
    case resp@HttpResponse(code, _, _, _) => {
      // TODO handle StatusCodes != OK and save log of error
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    }
    case _ => log.info(s"unhandled message received")
  }
}

// receives Seq[Record] and sends it to Spark object which saves
class Persister extends Actor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive: Receive = {
    case (data: Seq[Record], request: DataDownloadRequest) => {
      log.info(s"persisting ${data.length} records ${request.ticker}")
      SaveToCsv.save(request, data)
      //      Spark.save(request, data)
    }
    case _ => log.info("unhandled message received")
  }
}


object BootStrap extends App {

  override def main(args: Array[String]): Unit = {

    val actorSystem = ActorSystem("actor_system")

    val clientActor = actorSystem.actorOf(Props[FromFile], "consuming_system")
    clientActor ! "sadfa"

    println("main thread: continue on doing other work while data is downloaded/parsed")


    println("main thread: wait for Actor system termination")
    Thread.sleep(Timeout(10 seconds).duration.toMillis)


    println("main thread: time out.  stop akka")
    actorSystem.stop(clientActor)
    println("main thread: execution complete.  terminate actor system...")
    actorSystem.terminate()
  }

  class HardCodeTest extends Actor with ActorLogging {

    private val masterRef = context.actorOf(Props[DownloaderRootActor], name = "master_consumer")

    override def receive: Receive = {
      case _ => {
        log.info("beginning test")
        masterRef ! new PriceDownloadRequest("MSFT")
        masterRef ! new PriceDownloadRequest("FB")
        masterRef ! new PriceDownloadRequest("NKE")
        masterRef ! new PriceDownloadRequest("NKE")
      }
    }
  }

  class FromFile extends Actor with ActorLogging {

    val masterRef = context.actorOf(Props[DownloaderRootActor], name = "master_consumer")

    override def receive: Receive = {
      case _ => {
        log.info("beginning full download ...")
        makeRequests.foreach((request: DataDownloadRequest) =>
          masterRef.forward(request)
        )
      }
    }

    def makeRequests: Seq[PriceDownloadRequest] = {
      val stockFile = Source.fromFile(s"/Users/gcrowell/Documents/git/treasureapp/stock.txt")
      val requests = stockFile.getLines().map(_.split('|')(0)).filter(_.length > 0).map(new PriceDownloadRequest(_)).toSeq
      requests
    }
  }


}
