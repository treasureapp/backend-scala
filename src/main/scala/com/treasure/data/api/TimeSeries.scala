package com.treasure.data.api


import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.language.postfixOps

case class DataPoint(value: Double, dateTime: Date)

case object PriceDataReader extends LazyLogging {


  /**
    * path of csv is set by TypeSafe configuration: src/main/resources/application.conf
    */

  private val ss: SparkSession = SparkSession.builder
    .appName("My Spark Application")
    .master("local[*]")
    //    .enableHiveSupport()
    .config("spark.sql.warehouse.dir", "target/spark-warehouse")
    .getOrCreate

  import ss.implicits._
//  import ss.sqlContext.implicits._

  def getData(symbol: String): Dataset[DataPoint] = {
//    val personEncoder = Encoders.product[DataPoint]
//    personEncoder.
    priceDataSet.select($"date", $"close").as[DataPoint]
  }
  var isoDateFormat: DateFormat = new SimpleDateFormat("YYYY-mm-dd")
  private val readCsvFromPath: DataFrame = {
    val config = ConfigFactory.load()
    val pricePathStr = config.getString("treasure.data.price_file")
    val pricePath = scala.reflect.io.Path(pricePathStr)

    logger.debug(s"loading csv file from:\n${pricePath}")
    ss.read
      .option("header", true)
      .csv(pricePath.toAbsolute.toString)
  }

  val dataSetStr: Dataset[PriceStr] = {
    readCsvFromPath.withColumnRenamed("ex-dividend", "ex_dividend").as[PriceStr]
  }
  private val transformToDataSet: Dataset[Price] = {
//    val isoDateFormat = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")

    val price_str_ds = readCsvFromPath
      .map {
      case PriceStr(
      ticker: String,
      date: String,
      open: String,
      high: String,
      low: String,
      close: String,
      volume: String,
      ex_dividend: String,
      split_ratio: String,
      adj_open: String,
      adj_high: String,
      adj_low: String,
      adj_close: String,
      adj_volume: String
      ) => Price(
        ticker,
//        isoDateFormat.parse(date),
        date,
        open.toDouble,
        high.toDouble,
        low.toDouble,
        close.toDouble,
        volume.toDouble.round,
        ex_dividend.toDouble,
        split_ratio.toDouble,
        adj_open.toDouble,
        adj_high.toDouble,
        adj_low.toDouble,
        adj_close.toDouble,
        adj_volume.toDouble.round
      )
    }.as[Price]
  }

  case class Price(ticker: String,
                   date: String,
//                   date: Date,
                   open: Double,
                   high: Double,
                   low: Double,
                   close: Double,
                   volume: Long,
                   ex_dividend: Double,
                   split_ratio: Double,
                   adj_open: Double,
                   adj_high: Double,
                   adj_low: Double,
                   adj_close: Double,
                   adj_volume: Long
                  )

  case class PriceStr(ticker: String,
                              date: String,
                              open: String,
                              high: String,
                              low: String,
                              close: String,
                              volume: String,
                              ex_dividend: String,
                              split_ratio: String,
                              adj_open: String,
                              adj_high: String,
                              adj_low: String,
                              adj_close: String,
                              adj_volume: String
                             )

  val priceDataSet: Dataset[Price] = transformToDataSet
}

