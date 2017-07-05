package com.treasure.data

/**
  * Created by gcrowell on 2017-06-24.
  */
sealed trait Ticker {
  def ticker: String
}
/**
  * Record is place holder for a single datum
  */
sealed trait Record extends Product with Ticker {

  def dateId: Long

  def ticker: String

  override def toString: String = {
    this.toCSV(",")
  }

  def toCSV(sep: String): String = {
    this.productIterator.mkString(sep)
  }
}

/**
  * Concrete implementation of Record for price data
  *
  * @param dateId
  * @param ticker
  * @param open
  * @param high
  * @param low
  * @param close
  * @param volume
  */
case class PriceRecord(dateId: Long, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long) extends Record


/**
  * Generic placeholder for statement where the came from or what it describes
  */
@deprecated
trait Subject {
  def name: String
}

@deprecated
trait TradeableSubject extends Subject {
  def symbol: String
}

@deprecated
case class Stock(val symbol: String, val name: String = "") extends TradeableSubject

trait TextParser[A <: Record] {
  def parse(text: String): Seq[A]

  def toCSV(path: String): Unit = {
  }
}

trait DataDownloadRequest extends TextParser[Record] with Ticker {

  def urlString: String

  override def ticker: String


}