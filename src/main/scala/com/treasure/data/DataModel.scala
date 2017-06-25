package com.treasure.data

/**
  * Created by gcrowell on 2017-06-24.
  */

/**
  * Record is place holder for a data point/row/record
  */
sealed trait Record extends Product

/**
  * Concrete implementation of Record for price data
  * @param dateId
  * @param open
  * @param high
  * @param low
  * @param close
  * @param volume
  */
case class PriceRecord(dateId: Long, open: Double, high: Double, low: Double, close: Double, volume: Long) extends Record

/**
  * Generic placeholder for statement where the came from or what it describes
  */
trait Subject {
  def name: String
}

trait TradeableSubject extends Subject {
  def symbol: String
}

case class Stock(val symbol: String, val name: String = "") extends TradeableSubject

