package com.treasure.util

import java.util.Calendar

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

/**
  * Created by gcrowell on 2017-06-23.
  */

object Config extends LazyLogging {

  val config = ConfigFactory.load()

  val dataRootPath = config.getString("treasure.data.root")
  val priceRootPath = config.getString("treasure.data.price")
  val statementRootPath = config.getString("treasure.data.statement")
  val price_file = config.getString("treasure.data.price_file")
  val test_price_file = config.getString("treasure.data.test_price_file")

}

object Constants {
  private val _epoch = Calendar.getInstance()
  _epoch.set(1991, 10, 1)

  def epoch: Calendar = {
    _epoch
  }
}


case class DateValue(subjectId: String, dateId: Long, value: Double)

object DataLoader {
  def getData: Seq[DateValue] = {
    val rdr = Source.fromFile(Config.config.getString("treasure.data.test_price_data_file"))
    val iso_date_regex = "\\d{4}-\\d{2}-\\d{2}".r
    val number_regex = "\\d+".r
    rdr.getLines().map {
      (line: String) => {
        val tkns = line.split(",")
        val symbol = tkns(0)
        val dateId: Long = tkns(1) match {
          case iso_date_regex(m) => m.mkString.toLong
          case _ => -1
        }
        val value: Double = tkns(5) match {
          case number_regex(m) => tkns(5).toDouble
          case _ => -1.0
        }
        (symbol, dateId, value)
      }
    }.filter((tuple:(String, Long, Double)) => tuple._2 != -1 && tuple._3 != -1.0)
    .map((tuple:(String, Long, Double)) => DateValue(tuple._1, tuple._2, tuple._3)).toSeq
  }
}


