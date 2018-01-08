package com.treasure.data.model

class Schema {

}

trait Stock extends Asset {
  val priceTimeSeries: TimeSeries
  val exchange: Exchange
  val financialReport: FinancialReport
  val company: Company
}

trait Asset extends Ticker

trait Ticker {
  val symbol: String
}

trait TimeSeries {
  val timeSeries: Seq[DataPoint]
}

trait Exchange {
  sealed private trait EnumVal
  case object NYSE extends EnumVal
  case object TSX extends EnumVal
  case object NASDAQ extends EnumVal
}

trait FinancialReport {
  val IncomeStatement: Object
  val CashFlowStatement: Object
  val BalanceSheet: Object
}

trait Company {
  val name: String
  val industry: Industry
}

trait DataPoint {
  val name: Object
  val dateRange: DateRange
  val value: Double
  val asset: Asset
}

trait Industry {
  val name: String
  val sector: Sector
}

trait Sector {
  val industries: Seq[Industry]
  val name: String
}

trait DateRange




