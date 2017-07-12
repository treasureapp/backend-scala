package com.treasure.analytics

import java.io.FileNotFoundException

import com.treasure.util.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, SparkSession}

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
                 open: Float,
                 high: Float,
                 low: Float,
                 close: Float,
                 volume: Long,
                 exDividend: Float,
                 splitRatio: Float,
                 adjopen: Float,
                 adjHigh: Float,
                 adjLow: Float,
                 adjClose: Float,
                 adjVolume: Float
                )


object Foo extends App with LazyLogging {

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
      StructField("open", FloatType, false),
      StructField("high", FloatType, false),
      StructField("low", FloatType, false),
      StructField("close", FloatType, false),
      StructField("volume", LongType, false),
      StructField("exDividend", FloatType, false),
      StructField("splitRatio", FloatType, false),
      StructField("adjOpen", FloatType, false),
      StructField("adjHigh", FloatType, false),
      StructField("adjLow", FloatType, false),
      StructField("adjClose", FloatType, false),
      StructField("adjVolume", IntegerType, false)
    )
  )


  override def main(args: Array[String]): Unit = {

    /**
      * path of csv is set by TypeSafe configuration: src/main/resources/application.conf
      */
    val path = scala.reflect.io.Path(Config.dataRootPath + "/price_data.csv")
    logger.debug(s"loading csv file from:\n${path}")


    // check if file exists
    val csvExists = if (path.exists) {


      //***********************************
      // Load data
      //***********************************

      logger.info(s"File found.  Loading data ...")
      val data = loadFromFile(path.path)
      println(s"${data.count} data records loaded")

      //***********************************


    } else {
      logger.error(s"\n\ncsv file not found.  \nMove csv file to folder set in application.conf.  \nRename file to price_data.csv\n\n")
      throw new FileNotFoundException(s"csv from https://www.quandl.com/product/WIKIP/WIKI/PRICES-Quandl-End-Of-Day-Stocks-Info expected at ${path.path}")
    }


  }

  /**
    * 1. Creates spark session (ie starts up Spark)
    * 2. Load csv data into Spark Dataset
    * 3. Return Dataset[Price]
    */
  def loadFromFile(path: String): Dataset[Price] = {


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

    /**
      * import implicits to enable implicit conversion from Spark DataFrame to Dataset[Price]
      *
      * @see https://spark.apache.org/docs/latest/sql-programming-guide.html#starting-point-sparksession
      * @see https://spark.apache.org/docs/latest/sql-programming-guide.html#json-datasets
      */
    import ss.implicits._


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
      .as[Price] // convert DataFrame to Dataset of Price case classes

  }

}
