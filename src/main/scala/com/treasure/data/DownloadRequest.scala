package com.treasure.data

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by gcrowell on 2017-06-24.
  */


case class PriceDownloadRequest(val ticker: String, val startDate: Option[Calendar] = None) extends DataDownloadRequest {

  override def parse(text: String): Seq[PriceRecord] = parseCsvData(text)


  def parseCsvData(csv: String): Seq[PriceRecord] = {
    csv.split("\n").drop(1).map((csvLine: String) => parseCsvLine(csvLine)).toList
  }

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

    PriceRecord(yyyyMMdd.toLong, ticker, csv_tokens(1).toDouble, csv_tokens(2).toDouble, csv_tokens(3).toDouble, csv_tokens(4).toDouble, csv_tokens(5).toLong)
  }

  override def urlString: String = {
    s"https://www.google.com/finance/historical?output=csv&q=${ticker}&startdate=" + (startDate match {
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


  def toDateArg(date: Calendar): String = {
    //    http://www.google.ca/finance/historical?cid=358464&startdate=May+14%2C+2014&enddate=Jun+30%2C+2017&num=30&ei=UNdVWaD7C4H2jAHi8Kb4BA&output=csv
    s"${MMM_fmt.format(date.getTime)}+${date.get(Calendar.DAY_OF_MONTH)}%2C+${date.get(Calendar.YEAR)}"
  }
}

