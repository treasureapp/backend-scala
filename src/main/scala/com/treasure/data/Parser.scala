package com.treasure.data

import java.nio.charset.CodingErrorAction

import com.typesafe.scalalogging.LazyLogging

import scala.io.{Codec, Source}
import scala.xml.XML

/**
  * Created by gcrowell on 2017-06-22.
  */
trait Parser2 extends LazyLogging {

  protected def read(path: String): String = {
    logger.info(s"parsing file at $path")
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    val bufferedSource = Source.fromFile(path)
    val html = bufferedSource.getLines().mkString
    bufferedSource.close
    html
  }
}

trait HtmlFileParser extends Parser2 {

  def parse(path: String): Unit = {

  }

}

class NasdaqHtmlFileParser extends LazyLogging with HtmlFileParser {

  override def parse(path: String): Unit = {
    val html = super.read(path)
//    val goo = XML.LoadFile("")
//    val foo = html \ "td"
//    logger.info(s"${ }")

  }

}

object DemoNasdaqHtmlFileParser extends App {


  override def main(args: Array[String]): Unit = {
    val path = "NasdaqStatementData.html"

    val parser = new NasdaqHtmlFileParser
    parser.parse(path)
  }

}