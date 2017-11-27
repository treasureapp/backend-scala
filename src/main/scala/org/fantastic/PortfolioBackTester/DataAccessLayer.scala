
package org.fantastic.PortfolioBackTester

import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

import org.fantastic.DataModel.{Stock, StockOHLC}
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api.Database
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
//import yahoofinance.YahooFinance
//import yahoofinance.histquotes.Interval


abstract class UpdateTaskSpec

case class UpdatePriceTaskSpec(symbol: String, fromDate: Option[Int]) extends UpdateTaskSpec

case object PriceSync {

  def getOutDated(): Seq[UpdatePriceTaskSpec] = {
    import org.fantastic.util.DateUtils._
    val db = Database.forConfig("aws")
    val stock = TableQuery[Stock]
    val price = TableQuery[StockOHLC]
    val epoch = 19000101

    val leftOuterJoin = (for {
      (s, p) <- stock joinLeft price on (_.symbol === _.stock_symbol)
    } yield (s.symbol, p.map(_.date_id))).groupBy(_._1)

    val leftOuterJoinGroup = leftOuterJoin.map {
      case (s, p) => (s, p.map {
        _._2.getOrElse(epoch)
      }.max)
    }

    val expectedMostRecentDate = getMostRecentCompleteTradeDateId()
    val filterUpToDate = leftOuterJoinGroup.filter(_._2 < expectedMostRecentDate)

    // wait for query results to return
    val result = Await.result(
      db.run(filterUpToDate.result)
      , Duration.Inf
    )

    result.map {
      case (s, p) => UpdatePriceTaskSpec(s, p)
    }
  }

  //  def downloadData(task: UpdatePriceTaskSpec): Seq[(String, Int, Double, Double, Double, Double, Int, Double)] = {
  //    val stock = YahooFinance.get(task.symbol)
  //    val interval = Interval.DAILY
  //    val fromDate = task.fromDate.getOrElse(Common.epochDateId)
  //    val vector_HistoricalQuote = stock.getHistory(fromDate, interval).asScala
  //    val sql_rows = for (historicalQuote <- vector_HistoricalQuote) yield {
  //      val date_id = new SimpleDateFormat("yyyyMMdd").format(historicalQuote.getDate().getTime).toInt
  //      (historicalQuote.getSymbol(), date_id, historicalQuote.getOpen().doubleValue, historicalQuote.getHigh().doubleValue, historicalQuote.getLow().doubleValue, historicalQuote.getClose().doubleValue, historicalQuote.getVolume().intValue, historicalQuote.getAdjClose().doubleValue)
  //    }
  //    return sql_rows
  //  }
}
