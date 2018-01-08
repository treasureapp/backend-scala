package org.fantastic.Http

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.fantastic.DataModel.StockOHLCRecord
import org.fantastic.util.MetaTimeSeries

import scala.concurrent.Future

/**
  * Created by gcrowell on 2017-06-10.
  */


/**
  * HttpDownloadService is a background service that handles requests for csv price data from google finance (internet).
  * HttpDownloadService exposes it's getHttp method to clients that returns a Seq of price rows.
  * When app starts up HttpDownloadService creates it's own internal ActorSystem named HttpActorSystem.
  */
object HttpDownloadService {

  // create a new actor system
  private val actorSystem = ActorSystem("HttpActorSystem")

  def getHttp(request: MetaTimeSeries): Unit = {
    //Future[Seq[StockOHLCRecord]] = {

    val props: Props = HttpDownoader.buildProperty(request)

    val slaveActor: ActorRef = actorSystem.actorOf(props, request.ticker)

    val returnData: Future[Seq[StockOHLCRecord]] = {
      ???
    }


  }

}


class HttpDownoader(val url: String) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  val httpRequest = HttpRequest(uri = url)
  val http = Http(context.system)
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))


  override def preStart() = {
    http.singleRequest(httpRequest).pipeTo(self)
  }


  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + body.utf8String)



      }
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    case _ => ???
  }
}

object HttpDownoader {
  def buildProperty(request: MetaTimeSeries): Props = {
    val url = s"https://www.google.com/finance/historical?output=csv&q=${request.ticker}"
    Props(new HttpDownoader(url))
  }
}

