package com.treasure.analytics

import com.treasure.analytics.DemoSparkLoad.schema
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

/**
  * Created by gcrowell on 2017-07-19.
  */
case class State(subject: String, label: String, date: String, value: String)

object Loader {

  lazy val config = ConfigFactory.load()
  lazy val path = scala.reflect.io.Path(config.getString("treasure.data.price_file"))


  val spark = SparkSession.builder
    .appName("My Spark Application")
    .master("local[*]")
    //    .enableHiveSupport()
    .config("spark.sql.warehouse.dir", "target/spark-warehouse")
    .getOrCreate


  val dataFrame: DataFrame = spark.read // creates DataFrameReader
    .option("header", true)
    .schema(schema) // explicitly set meta data @todo this has no effect
    .csv(path.toCanonical.toString) // executes reader, returns DataFrame

  println(dataFrame.columns.toString)

  import spark.implicits._

  dataFrame.columns.foreach((column_name: String) => println(dataFrame.select(column_name).distinct()))
  dataFrame.columns.map(dataFrame.columns.indexOf(_)).foreach(println)
//  dataFrame.columns.map(dataFrame.columns(_)).foreach(println)


  val filterFunc = (row: Row) =>
    if (
      !row.isNullAt(0) &&
        !row.isNullAt(1) &&
        !row.isNullAt(5)
    ) true else false

  val dataset: Dataset[State] = dataFrame.filter(filterFunc)
    .map {
      case Row(ticker: String, date: String, _, _, _, close: String, _, _, _, _, _, _, _, _) => State(ticker, "price", date, close)
    }.as[State]

  println(dataset.count())
}

object simpleMovingAverage {

  val spark = SparkSession.builder
    .appName("My Spark Application")
    .master("local[*]")
    //    .enableHiveSupport()
    .config("spark.sql.warehouse.dir", "target/spark-warehouse")
    .getOrCreate

  val window = Window.partitionBy("subject").orderBy("date").rowsBetween(-50, 0)

  def simpleMovingAverage(dataset: Dataset[State]): Unit = {
    dataset.withColumn("simpleMovingAverage", avg(dataset("value")).over(window)).show()
  }
}

object Start extends App {


  override def main(args: Array[String]): Unit = {
    val ds = Loader.dataset
    simpleMovingAverage.simpleMovingAverage(ds)
  }
}