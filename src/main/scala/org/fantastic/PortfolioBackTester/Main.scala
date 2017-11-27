/**
  * Created by gcrowell on 4/28/2017.
  */

package org.fantastic.PortfolioBackTester


import java.text.SimpleDateFormat

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import java.util.Calendar

import org.fantastic.DataModel.{Stock, StockOHLC}


object Tests {

  def getDb(): Database = {
    Database.forConfig("aws")
  }

  def SlickFinDwAws(): Unit = {
    println("connecting to local postgres using TypeSafe configuration file: application.conf")
    val db = Database.forConfig("aws")
    try {
      println("Create the tables, including primary and foreign keys")
      val setupFuture = db.run(FinDwSchema.setup)
    } finally db.close

    println("tables created in postgreSql instance running locally")
    println("see <database>.<schema>: findw.public")
  }

  def SlickFinDwLocal(): Unit = {
    println("connecting to local postgres using TypeSafe configuration file: application.conf")
    val db = Database.forConfig("local")
    try {
      println("Create the tables, including primary and foreign keys")
      val setupFuture = db.run(FinDwSchema.setup)
    } finally db.close

    println("tables created in postgreSql instance running locally")
    println("see <database>.<schema>: findw.public")
  }

//  def UploadPriceData(symbol: String): Unit = {
//    println("connecting to local postgres using TypeSafe configuration file: application.conf")
//    val db = Database.forConfig("local")
//    try {
//      println(s"upload price data for $symbol")
//      val setupFuture = db.run(FinDwSchema.uploadData(symbol))
//    } finally db.close
//
//    println("tables created in postgreSql instance running locally")
//    println("see <database>.<schema>: findw.public")
//  }


  //  http://queirozf.com/entries/slick-3-reference-and-examples
  def AwaitDbQuery: Unit = {

    val db = Database.forConfig("aws")
    val stock = TableQuery[Stock]

    Await.result(
      db.run(stock.map(_.name).result).map { res =>
        println(res)
      }, Duration.Inf)
  }

  def linkMovingAverages: Unit = {

    val a = simpleMovingAverage(5, "AA")
    val b = simpleMovingAverage(10, "AA")

    println(a) //returns empty list

    //create a new map with key being dates (._2) with a second Int tuple of 1 or 0 depending if a > b

  }

  def simpleMovingAverage(period: Int, name: String): Unit = {
    //connect to db and setup stock and price table
    val db = Database.forConfig("aws")
    val stock = TableQuery[Stock]
    val price = TableQuery[StockOHLC]

    //output: adjusted close, symbol, date
    val q2 = for {
      c <- price if c.stock_symbol === name
    } yield (c.adjusted_close, c.stock_symbol, c.date_id)

    //output: future[tuple2]
    val q1 = q2.map {
      case (s, p, t) => (s, t)
    }

    //function to get the moving average for each date
    //input: Sequence[Tuple2]??
    //output: sum of sequence of tuples, max date. Tuple2
    def listofSeqTuple(list: Seq[Tuple2[Double, Int]]): Tuple2[Double, Int] = {
      list.unzip match {
        case (l1, l2) => (l1.sum / period, l2.max)
      } //tuples containing values < period would not be correct (mva of 5 would not have first 5 dates)
    }

    //input -> window of values -> moving average
    //input: future[tuple2]
    //output: List[tuple2]
    Await.result(
      db.run(
        q1.result).map { res =>
        println(res)
        //maps to Vector and prints results
        val q3 = res.toList
        // create list of tuple2 [(Double,Int)]
        val q4 = q3.iterator.sliding(period).toList
        //create list of the tuples based on the size of period
        //println(q4)
        val q5 = q4.map {
          listofSeqTuple
        } //List[Tuple2[Double,Int]] is this the only thing returned? how do we return it for further analysis?
        println(q5)
      }
      , Duration.Inf
    )
  }

  def getExpectedMaxDate: Int = {
    val now = Calendar.getInstance()
    val offset = getOffset(now)
    now.add(Calendar.DAY_OF_MONTH, offset)
    //
    val s = new SimpleDateFormat("yyyyMMdd")
    s.format(now.getTime()).toInt
  }

  def getOffset(today: Calendar): Int = {
    if (today.get(Calendar.DAY_OF_WEEK) == 1) -2
    else if (today.get(Calendar.DAY_OF_WEEK) == 2) -3
    else if (today.get(Calendar.HOUR_OF_DAY) < 13) -1
    else 0
  }

  /**
    *
    * get most recent date id for each stock in price table
    *
    */
  def priceQueries: Unit = {

    val db = Database.forConfig("aws")
    val stock = TableQuery[Stock]
    val price = TableQuery[StockOHLC]

    /**
      * sequence of chained query-like expressions
      */
    def leftJoin(): Unit = {
      val leftOuterJoin = for {
        s <- stock
        p <- price
        if s.symbol === p.stock_symbol

      } yield (s.symbol, p.date_id)

      val leftOuterJoinGrouped = (for {
        s <- stock
        p <- price
        if s.symbol === p.stock_symbol

      } yield (s, p)).groupBy(_._1.symbol)

      val leftOuterJoinAgg = leftOuterJoinGrouped.map {
        case (s, p) => (s, p.map {
          _._2.date_id
        }.max)
      }

      // DISINTCT SYMBOL
      Await.result(
        db.run(
          leftOuterJoin.map(_._1).distinct.result).map { res =>
          println(res)
        }
        , Duration.Inf

      )

      // MAX DATE GROUP BY SYMBOL
      Await.result(
        db.run(
          leftOuterJoinAgg.result).map { res =>
          println(res)
        }
        , Duration.Inf
      )
    }

    leftJoin()

    // all stock names
    Await.result(
      db.run(stock.map(_.name).result).map { res =>
        println(res)
      }, Duration.Inf)
  }

  def isCompletedDbQuery: Unit = {
    // database config
    val db = Database.forConfig("aws")
    // declare a query against org.fantastic.findw.Stock table
    val stock = TableQuery[Stock]
    // initiate/begin the query
    // runit is an instance of "Future"
    val runit = db.run(stock.map(_.name).result).map { res =>
      println(res)
    }

    // check if query has finished executing
    while (!runit.isCompleted) {
      // if query not complete then wait for 1000 ms and check again
      println("waiting for query")
      Thread.sleep(1000L)
    }
  }
}


object Main {

  def main(args: Array[String]): Unit = {
    println(s"program starting")
    val start = System.currentTimeMillis()
    println(s"current working directory: ${System.getProperty("user.dir")}")

    for(arg <- args) {
      println(s"arg=$arg")
    }
//    test()
//    QuesTradeDataGet.foo()

    val end = System.currentTimeMillis()
    println(s"execution complete ${end - start} ms")
  }

  def test(): Unit = {
    //    Tests.SlickAws()
    //    Tests.HttpDownload()
    //    Tests.YahooLib()
    //    Tests.UpdateData()
    //    val x = Tests.QueryDb
    //    val y = Tests.WaitForQuery
    //    Tests.SlickFinDwLocal
    //    Tests.SlickFinDwAws()
    //    Tests.UploadPriceData("A")
    //    Tests.UpdateAllStockPriceData
    //    Tests.priceQueries
    //Tests.simpleMovingAverage(5,"AA")
    //PriceSync.getOutDated()
  }

}
