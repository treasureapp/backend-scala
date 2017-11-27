////import java.util.Calendar
////import java.util.function.Consumer
////import java.{text, util}
////import java.text.{DateFormat, SimpleDateFormat}
////
////import FinDwSchema.StockOHLC
////import slick.lifted.TableQuery
////import yahoofinance.YahooFinance
////import yahoofinance.histquotes.{HistoricalQuote, Interval}
////import slick.jdbc.PostgresProfile.api._
////import scala.collection.JavaConverters._
////
//
//
//package org.fantastic.PortfolioBackTester
//
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import yahoofinance.YahooFinance
//import yahoofinance.histquotes.Interval
//import scala.collection.JavaConverters._
///**
//  * Created by gcrowell on 2017-05-24.
//  */
///**
//  * Downloads price data
//  * @param symbol
//  */
//class PriceIO(symbol: String = "INTC") {
//
//  /**
//    * Return collection of tuples price data.  One element per day
//    * @param year
//    * @param month
//    * @param day
//    * @return
//    */
//  def getData(year: Int = 1900, month: Int = 1, day: Int = 1): Seq[Tuple8[String, Int, Double, Double, Double, Double, Int, Double]] = {
//    var start = Calendar.getInstance()
//    start.set(year, month, day)
//
//    // TODO: function that checks if symbol exists in database
//    val stock = YahooFinance.get(symbol)
//    println(stock.print)
//
//    val interval = Interval.DAILY
//    println(interval)
//    val vector_HistoricalQuote = stock.getHistory(start, interval).asScala
//    val sql_rows = for (historicalQuote <- vector_HistoricalQuote) yield {
//      val date_id = new SimpleDateFormat("yyyyMMdd").format(historicalQuote.getDate().getTime).toInt
//      (historicalQuote.getSymbol(), date_id, historicalQuote.getOpen().doubleValue, historicalQuote.getHigh().doubleValue, historicalQuote.getLow().doubleValue, historicalQuote.getClose().doubleValue, historicalQuote.getVolume().intValue, historicalQuote.getAdjClose().doubleValue)
//    }
//    return sql_rows
//  }
//}