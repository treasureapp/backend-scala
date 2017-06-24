package com.treasure.data

/**
  * Created by gcrowell on 2017-06-23.
  */

import java.text.SimpleDateFormat
import java.util.Calendar

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


/**
  * Record is place holder for a data point/row/record
  */
sealed trait Record

/**
  * Concrete implementation of Record for price data
  * @param dateId
  * @param open
  * @param high
  * @param low
  * @param close
  * @param volume
  */
case class PriceRecord(dateId: Long, open: Double, high: Double, low: Double, close: Double, volume: Long) extends Record


/**
  * Generic placeholder for statement where the came from or what it describes
  */
trait Subject {
  def name: String
}

trait TradeableSubject extends Subject {
  def symbol: String
}

case class Stock(val symbol: String, val name: String = "") extends TradeableSubject





trait TextParser[A <: Record] {
  def parse(text: String): Seq[A]
}

trait DataDownloadRequest extends TextParser[Record] {

  def urlString: String

  def subject: Subject

}


/**
  * Root/Master
  * doesn't do anything.  just receives and forwards/routes DataDownloadRequest's to 1 of it's slaves.
  */
class Master extends Actor with ActorLogging {

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


case class PriceDownloadRequest(val stock: TradeableSubject, val startDate: Option[Calendar] = None) extends DataDownloadRequest {

  override def subject: Subject = stock

  override def parse(text: String): Seq[PriceRecord] = parseCsvData(text)


  def parseCsvData(csv: String): Seq[PriceRecord] = {
    csv.split("\n").drop(1).map((csvLine: String) => PriceDownloadRequest.parseCsvLine(csvLine)).toList
  }

  override def urlString: String = {
    s"https://www.google.com/finance/historical?output=csv&q=${stock.symbol}&startdate=" + (startDate match {
      case dt: Some[Calendar] => PriceDownloadRequest.toDateArg(dt.get)
      case _ => PriceDownloadRequest.toDateArg(Constants.epoch)
    })
  }

  class PriceParser extends TextParser[PriceRecord] {
    override def parse(text: String): Seq[PriceRecord] = parseCsvData(text)
  }

}

object PriceDownloadRequest {
  val MMM_fmt = new SimpleDateFormat("MMM")

  def parseCsvLine(csvLine: String): PriceRecord = {

    val month_map = Map(
      "Jan" -> "01",
      "Feb" -> "02",
      "Mar" -> "03",
      "Apr" -> "04",
      "May" -> "05",
      "Jun" -> "06",
      "Jul" -> "07",
      "Aug" -> "08",
      "Sep" -> "09",
      "Oct" -> "10",
      "Nov" -> "11",
      "Dec" -> "12"
    )

    val csv_tokens = csvLine.split(",")
    val date_tokens = csv_tokens(0).split("-")

    val yyyyMMdd = (
      date_tokens(2) match {
        case x if (x < "10") => s"200$x"
        case x if (x < "20") => s"20$x"
        case x if (x > "20") => s"19$x"
        case _ => "9999"
      }) + (
      month_map.getOrElse(date_tokens(1), "00")
      ) + (
      date_tokens(0) match {
        case x if (x < "10") => s"0$x"
        case x if (x > "10") => x
        case _ => "31"
      })

    PriceRecord(yyyyMMdd.toLong, csv_tokens(1).toDouble, csv_tokens(2).toDouble, csv_tokens(3).toDouble, csv_tokens(4).toDouble, csv_tokens(5).toLong)
  }

  def toDateArg(date: Calendar): String = {
    s"${MMM_fmt.format(date.getTime)}+%2C+${date.get(Calendar.DAY_OF_MONTH)}+${date.get(Calendar.YEAR)}"
  }
}


object DemoDownloader extends App {

  override def main(args: Array[String]): Unit = {
    class Testing123 extends Actor with ActorLogging {

      private val masterRef = context.actorOf(Props[Master], name = "master_consumer")

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
    Thread.sleep(3000)

    println("execution complete.  stopping actor system...")
    actorSystem.terminate()
  }
}
