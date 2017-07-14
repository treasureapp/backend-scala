package com.treasure.analytics

import java.io.FileNotFoundException

import com.treasure.util.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.language.postfixOps

/**
  * ******************************************************************
  *
  * Load monolithic csv price data file into Spark Dataset[Price]
  *
  * ******************************************************************
  *
  * @see Good overview of loading data with Spark is at
  *      https://spark.apache.org/docs/latest/sql-programming-guide.html
  * @see RDD vs DataFrame vs Dataset
  *      https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html
  * @see data source
  *      https://www.quandl.com/product/WIKIP/WIKI/PRICES-Quandl-End-Of-Day-Stocks-Info
  *      Created by gcrowell on 2017-07-11.
  */


// each record in Dataset is an instance of Price
case class Price(ticker: String,
                 date: String,
                 open: Double,
                 high: Double,
                 low: Double,
                 close: Double,
                 volume: Long,
                 exDividend: Double,
                 splitRatio: Double,
                 adjopen: Double,
                 adjHigh: Double,
                 adjLow: Double,
                 adjClose: Double,
                 adjVolume: Long
                )

case class Goo(x: String, y: String)

//
//object conversions {
//  def toPrice(tkns: Array[String]): Option[Price] = {
//    try {
//      Some(Price(
//        ticker = tkns(0),
//        date = tkns(1),
//        open = tkns(2).toDouble,
//        high = tkns(3).toDouble,
//        low = tkns(4).toDouble,
//        close = tkns(5).toDouble,
//        volume = tkns(6).toDouble.round,
//        exDividend = tkns(7).toDouble,
//        splitRatio = tkns(8).toDouble,
//        adjopen = tkns(9).toDouble,
//        adjHigh = tkns(10).toDouble,
//        adjLow = tkns(11).toDouble,
//        adjClose = tkns(12).toDouble,
//        adjVolume = tkns(13).toDouble.round
//      ))
//    } catch {
//      Some(Price("dfa","Dfa",1.,2.,3.,4.,5,6,7,8,9,0,1,2,3))
//    }
//  }


//}

object DemoSparkLoad extends App with LazyLogging {


  /**
    * Explicitly define meta data information of csv data.
    *
    * @note this is required because csv files don't include meta data
    * @see https://stackoverflow.com/a/42679059/5154695
    *
    */
  val schema = StructType(
    Seq(
      StructField("ticker", StringType, false),
      StructField("date", StringType, false),
      StructField("open", DoubleType, false),
      StructField("high", DoubleType, false),
      StructField("low", DoubleType, false),
      StructField("close", DoubleType, false),
      StructField("volume", LongType, false),
      StructField("exDividend", DoubleType, false),
      StructField("splitRatio", DoubleType, false),
      StructField("adjOpen", DoubleType, false),
      StructField("adjHigh", DoubleType, false),
      StructField("adjLow", DoubleType, false),
      StructField("adjClose", DoubleType, false),
      StructField("adjVolume", LongType, false)
    )
  )

  /**
    * 1. Start SparkSession
    *
    * @todo wtf do these settings mean/do?
    */
  val ss: SparkSession = SparkSession.builder
    .appName("My Spark Application")
    .master("local[*]")
    //    .enableHiveSupport()
    .config("spark.sql.warehouse.dir", "target/spark-warehouse")
    .getOrCreate

  override def main(args: Array[String]): Unit = {


    /**
      * path of csv is set by TypeSafe configuration: src/main/resources/application.conf
      */
    val path = scala.reflect.io.Path(Config.dataRootPath + "/test_price_data.csv")
    logger.debug(s"loading csv file from:\n${path}")


    // check if file exists
    val csvExists = if (path.exists) {


      //***********************************
      // Load data
      //***********************************

      logger.info(s"File found.  Loading data ...")
      val price_df: DataFrame = readCsvFromPath(path.path)
      println(s"${price_df.count} data records loaded")
      println(price_df.printSchema())

      //***********************************

      /**
        * @todo convert to Dataset here
        *       etl the data into Dataset[Price]
        */
      val price_ds = toPriceDs(price_df)
      //      println(price_ds.printSchema())

    } else {
      logger.error(s"\n\ncsv file not found.  \nMove csv file to folder set in application.conf.  \nRename file to price_data.csv\n\n")
      throw new FileNotFoundException(s"csv from https://www.quandl.com/product/WIKIP/WIKI/PRICES-Quandl-End-Of-Day-Stocks-Info expected at ${path.path}")
    }


  }

  def toPriceDs(price_df: DataFrame): Unit = {
    price_df.printSchema()

    import ss.implicits._

    /**
      * @todo
      */
    price_df.map {
      case Row(
      ticker: String,
      date: String,
      open: String,
      high: String,
      low: String,
      close: String,
      volume: String,
      exDividend: String,
      splitRatio: String,
      adjopen: String,
      adjHigh: String,
      adjLow: String,
      adjClose: String,
      adjVolume: String)
      =>
        Price(
          ticker,
          date,
          open.toDouble,
          high.toDouble,
          low.toDouble,
          close.toDouble,
          volume.toDouble.round,
          exDividend.toDouble,
          splitRatio.toDouble,
          adjopen.toDouble,
          adjHigh.toDouble,
          adjLow.toDouble,
          adjClose.toDouble,
          adjVolume.toDouble.round)
    }.as[Price]
  }

  //
  //  def start(inputDf: DataFrame): Dataset[Price] = {
  //    //      import ss.implicits._
  //
  //    inputDf.map {
  //      case price: Price =>
  //      case Row(
  //      ticker: String,
  //      date: String,
  //      open: Double,
  //      high: Double,
  //      low: Double,
  //      close: Double,
  //      volume: Long,
  //      exDividend: Double,
  //      splitRatio: Double,
  //      adjopen: Double,
  //      adjHigh: Double,
  //      adjLow: Double,
  //      adjClose: Double,
  //      adjVolume: Double
  //      ) => Price(
  //        ticker,
  //        date,
  //        open,
  //        high,
  //        low,
  //        close,
  //        volume,
  //        exDividend,
  //        splitRatio,
  //        adjopen,
  //        adjHigh,
  //        adjLow,
  //        adjClose,
  //        adjVolume
  //      )
  //      case row: Row =>
  //      case _ =>
  //    }.as[Price]
  //  }

  /**
    * Load csv data into Spark DataFrame
    */
  def readCsvFromPath(path: String): DataFrame = {


    /**
      * import implicits to enable implicit conversion from Spark DataFrame to Dataset[Price]
      *
      * @see https://spark.apache.org/docs/latest/sql-programming-guide.html#starting-point-sparksession
      * @see https://spark.apache.org/docs/latest/sql-programming-guide.html#json-datasets
      */
    /**
      * @note only required when using implicit conversions DataFrame -> DataSet
      */
    // import ss.implicits._


    /**
      * Spark RDD as unstructured immutable distributed collection of elements (no columns, no type info)
      * Spark DataFrame stores data as a table with named columns.
      * Spark Dataset stores data as collection of typed (eg Price) elements.
      *
      * DataFrame are considered untyped.
      * since 2.0 DataFrame are implemented as a Dataset[Row]
      *
      * @see https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html
      * @see https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
      */
    ss.read // creates DataFrameReader
      .option("header", true)
      .schema(schema) // explicitly set meta data
      .csv(path) // executes reader, returns DataFrame
  }
}
