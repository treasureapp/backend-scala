package org.fantastic.PortfolioBackTester

/**
  * Created by gcrowell on 2017-06-01.
  */


import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model._
import akka.util.ByteString
import akka.http._
import akka.http.scaladsl._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.util._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import scala.concurrent.duration._

import scala.concurrent.Await

object QuesTradeDataGet {

  def foo(): Unit = {


    // construct a simple GET request to `homeUri`
    val uri = Uri("https://api01.iq.questrade.com/v1/symbols/search?prefix=BMO")
    val host = Host("api01.iq.questrade.com")
    val token = "Txh8KvxugQBcS69HQwA4rL6UewlBUg"
    val auth = headers.Authorization(OAuth2BearerToken(token))
    val request = HttpRequest(GET, uri = uri, headers = List(auth))
//    val responce = Http().singleRequest(request)


    // http://eleansa.blogspot.ca/2016/01/akka-http-client-example.html
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

    import system.dispatcher

    val response = Await.result(responseFuture, 5.seconds)

    response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String).foreach(println)

    Http().shutdownAllConnectionPools().onComplete(_ => system.terminate())
  }


  //  println(responce)
  //  println(responce.)


  // https://gist.github.com/dcaoyuan/f2f70bed35c647a4d9a43dfdbcb6dbb8
  //  def foo(): Unit = {
  //    val uri = "http://www.yahoo.com"
  //    val reqEntity = Array[Byte]()
  //
  //    val respEntity = for {
  //      request <- Marshal(reqEntity).to[RequestEntity]
  ////      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = uri, entity = request))
  //
  //      response <- Http().singleRequest(request)
  //      entity <- Unmarshal(response.entity).to[ByteString]
  //    } yield entity
  //
  //    val payload = respEntity.andThen {
  //      case Success(entity) =>
  //        s"""{"content": "${entity.utf8String}"}"""
  //      case Failure(ex) =>
  //        s"""{"error": "${ex.getMessage}"}"""
  //    }
  //  }
}

