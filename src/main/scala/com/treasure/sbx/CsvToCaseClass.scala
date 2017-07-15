package com.treasure.sbx

/**
  * Created by gcrowell on 2017-07-15.
  */


import com.treasure.util.Config

import scala.io.Source

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
    val csv_line = "^(\\w+),(\\d{4}-\\d{2}-\\d{2}),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+),(\\d+\\.\\d+)".r
    rdr.getLines.map {
      case csv_line(symbol, date, open, high, low, close, volume, _*) => {
        Some(symbol, date.split('-').mkString.toLong, open.toDouble, high.toDouble, low.toDouble, close.toDouble, volume.toDouble.round)
      }
      case _ => None
    }.toSeq
  }

  def toPricePoint(tupleData: Seq[Option[Tuple7[String, Long, Double, Double, Double, Double, Long]]]): Seq[PricePoint] = {
    tupleData.flatten.map(TupleToPricePoint.convert)
  }

}


object DemoCsvConversion extends App {
  override def main(args: Array[String]): Unit = {

    val data = DataLoader.getData
    println(data)
    data.foreach(println)

    val flatData = data.flatten
    println(flatData)
    flatData.foreach(println)

    val seqPricePoint = flatData.map(TupleToPricePoint.convert)
    println(seqPricePoint)
    seqPricePoint.foreach(println)



  }
}