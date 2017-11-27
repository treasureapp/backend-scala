package org.fantastic.DataModel

import org.fantastic.util.Asset
import slick.jdbc.PostgresProfile.api._

/**
  * Created by gcrowell on 2017-05-24.
  */


// Definition of the stock table (always use lowercase and _)
class Stock(tag: Tag) extends Table[(String, String, Option[String], Option[String], Option[Int])](tag, Some("dim"), "stock") {
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (ticker, name, currency, exchange, nasdaq_id)

  def ticker = column[String]("ticker", O.PrimaryKey) // This is the primary key column

  def name = column[String]("company_name") // follows any standard?

  // Option specifies NULLABLE
  def currency = column[Option[String]]("usd") // ISO standard?

  // Option specifies NULLABLE
  def exchange = column[Option[String]]("stock_exchange") // and standard?

  def nasdaq_id = column[Option[Int]]("nasdaq_id") // used to get financial statement data (eg http://fundamentals.nasdaq.com/nasdaq_fundamentals.asp?CompanyID=8244&NumPeriods=50&Duration=1&documentType=1)
}


//http://slick.lightbend.com/doc/3.2.0/schemas.html#mapped-tables
case class StockOHLCRecord(val ticker: String,
                           val dateId: Int,
                           val open: Double,
                           val high: Double,
                           val low: Double,
                           val close: Double,
                           val volume: Long,
                           val adjusted_close: Double
                          ) extends Asset {
}

// Definition of the stock_ohlc table
class StockOHLC(tag: Tag) extends Table[StockOHLCRecord](tag, Some("fact"), "stock_ohlc") {
  def * = (ticker, date_id, open, high, low, close, volume, adjusted_close) <> (StockOHLCRecord.tupled, StockOHLCRecord.unapply)

  def open = column[Double]("open")

  def high = column[Double]("high")

  // TODO: fix foreign key
  // A reified foreign key relation that can be navigated to create a join

  def low = column[Double]("low")

  def close = column[Double]("close")

  def volume = column[Long]("volume")

  def adjusted_close = column[Double]("adjusted_close")

  def pk = primaryKey("pk_stockohlc", (ticker, date_id))

  def ticker = column[String]("ticker")

  def date_id = column[Int]("date_id")

}

object Schema {
  val stock = TableQuery[Stock]
  val stockohlc = TableQuery[StockOHLC]
  // create table(s)
  val setup = DBIO.seq((stock.schema ++ stockohlc.schema).create)

}