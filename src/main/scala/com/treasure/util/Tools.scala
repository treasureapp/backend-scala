package com.treasure.util

import java.util.Calendar

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source
import scala.reflect.io.Path

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
  val out_path: Path = Config.dataRootPath.concat("price_parquet")

  def delete_out(out_path: Path): Unit = {
    if (out_path.exists) {
      out_path.deleteRecursively()
    }
  }
}

object Constants {
  private val _epoch = Calendar.getInstance()
  _epoch.set(1991, 10, 1)

  def epoch: Calendar = {
    _epoch
  }
}


case class DateValue(subjectId: String, dateId: Long, value: Double)

object TupleToPricePoint {
  def convert(tupleData: Tuple7[String, Long, Double, Double, Double, Double, Long]): PricePoint = {
    PricePoint(tupleData._1, tupleData._2, tupleData._3, tupleData._4, tupleData._5, tupleData._6, tupleData._7)
  }
}

case class PricePoint(subjectId: String, dateId: Long, open: Double, high: Double, low: Double, close: Double, volumne: Long)

object DataLoader {
  def getData: Seq[Option[Tuple7[String, Long, Double, Double, Double, Double, Long]]] = {
    val rdr = Source.fromFile(Config.test_price_file)
    val iso_date_regex = "\\d{4}-\\d{2}-\\d{2}".r
    val number_regex = "\\d+".r
    val csv_line = "^(\\w+),(\\d{4}-\\d{2}-\\d{2})(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)(,\\d+\\.\\d+)".r
    rdr.getLines.drop(1).map {
      case csv_line(symbol, date, open, high, low, close, volume, _*) => {
        Some(symbol, date.split('-').mkString.toLong, open.drop(1).toDouble, high.drop(1).toDouble, low.drop(1).toDouble, close.drop(1).toDouble, volume.drop(1).toDouble.round)
      }
      case _ => None
    }.toSeq
  }

  def toPricePoint(tupleData: Seq[Option[Tuple7[String, Long, Double, Double, Double, Double, Long]]]): Seq[PricePoint] = {
    tupleData.flatten.map(TupleToPricePoint.convert)
  }

}


