package com.treasure.data

/**
  * Created by gcrowell on 2017-06-24.
  */

/**
  * Ticker uniquely identifies some Asset
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
sealed case class PriceRecord(dateId: Long, ticker: String, open: Double, high: Double, low: Double, close: Double, volume: Long) extends Record

/**
  * parses raw text into into Seq[Record] (ie. a data table)
  *
  * @tparam A any type that implements the Record trait
  */
trait TextParser[A <: Record] {
  def parse(text: String): Seq[A]
}

/**
  * encapsulates all information required to download and parse into Seq[Record]
  */
trait DataDownloadRequest extends TextParser[Record] with Ticker {

  def urlString: String

  override def ticker: String

}

