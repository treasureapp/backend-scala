package com.treasure.analytics

import java.io.FileNotFoundException

import com.treasure.util.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

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
//case class Price(ticker: String,
//                 date: String,
//                 open: Option[Double],
//                 high: Option[Double],
//                 low: Option[Double],
//                 close: Option[Double],
//                 volume: Option[Long],
//                 exDividend: Option[Double],
//                 splitRatio: Option[Double],
//                 adjopen: Option[Double],
//                 adjHigh: Option[Double],
//                 adjLow: Option[Double],
//                 adjClose: Option[Double],
//                 adjVolume: Option[Long]
//                )
case class Price(ticker: String,
                 date: String,
                 open: Option[String],
                 high: Option[String],
                 low: Option[String],
                 close: Option[String],
                 volume: Option[String],
                 exDividend: Option[String],
                 splitRatio: Option[String],
                 adjopen: Option[String],
                 adjHigh: Option[String],
                 adjLow: Option[String],
                 adjClose: Option[String],
                 adjVolume: Option[String]
                )


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


  override def main(args: Array[String]): Unit = {

    /**
      * Start SparkSession
      */
    val ss: SparkSession = SparkSession.builder
      .appName("My Spark Application")
      .master("local[*]")
      //    .enableHiveSupport()
      .config("spark.sql.warehouse.dir", "target/spark-warehouse")
      .getOrCreate

    /**
      * set logging level.
      */
    //    LogManager.getRootLogger.setLevel(Level.WARN)

    /**
      * path of csv is set by TypeSafe configuration: src/main/resources/application.conf
      */
    val path = scala.reflect.io.Path(Config.price_file)
    logger.debug(s"loading csv file from:\n${path}")


    // check if data file exists
    val csvExists = if (path.exists) {


      //***********************************
      // Load data
      //***********************************

      logger.info(s"File found.  Loading data into DataFrame...")
      val price_df: DataFrame = readCsvFromPath(path.path, ss)
      println(s"${price_df.count} data records loaded")
      price_df.printSchema()

      //***********************************

      /**
        * etl the data into Dataset[Price]
        */
      logger.info(s"converting DataFrame -> Dataset[Price]")
      val price_ds = toPriceDs(price_df, ss)
      println(s"${price_ds.count} data records loaded")
      price_ds.printSchema()

    } else {
      logger.error(s"\n\ncsv file not found.  \nMove csv file to folder set in application.conf.  \nRename file to ${path.toCanonical}\n\n")
      throw new FileNotFoundException(s"csv from https://www.quandl.com/product/WIKIP/WIKI/PRICES-Quandl-End-Of-Day-Stocks-Info expected at ${path.path}")
    }
  }

  def toPriceDs(price_df: DataFrame, ss: SparkSession): Dataset[Tuple1[Price]] = {

    import ss.implicits._

    price_df.map {
      case Row(
      ticker: String,
      date: String,
      open: Option[String],
      high: Option[String],
      low: Option[String],
      close: Option[String],
      volume: Option[String],
      exDividend: Option[String],
      splitRatio: Option[String],
      adjopen: Option[String],
      adjHigh: Option[String],
      adjLow: Option[String],
      adjClose: Option[String],
      adjVolume: Option[String])
      =>
        Tuple1(Price(
          ticker,
          date,
          Option(open.getOrElse("0.0")),
          Option(high.getOrElse("0.0")),
          Option(low.getOrElse("0.0")),
          Option(close.getOrElse("0.0")),
          Option(volume.getOrElse("0.0")),
          Option(exDividend.getOrElse("0.0")),
          Option(splitRatio.getOrElse("0.0")),
          Option(adjopen.getOrElse("0.0")),
          Option(adjHigh.getOrElse("0.0")),
          Option(adjLow.getOrElse("0.0")),
          Option(adjClose.getOrElse("0.0")),
          Option(adjVolume.getOrElse("0.0"))
        ))
      case _ => Tuple1(null)
    }.as[Tuple1[Price]]
  }

  /**
    * Load csv data into Spark DataFrame
    */
  def readCsvFromPath(path: String, ss: SparkSession): DataFrame = {


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
      .schema(schema) // explicitly set meta data @todo has no effect
      .csv(path) // executes reader, returns DataFrame
  }
}
