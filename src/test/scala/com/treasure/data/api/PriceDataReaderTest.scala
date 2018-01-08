package com.treasure.data.api

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class PriceDataReaderTest extends FunSpec with BeforeAndAfter with Matchers {
  describe("PriceDataReader") {
    it("priceDataSet should have 100k records") {
      PriceDataReader.priceDataSet.printSchema()
      assert(PriceDataReader.priceDataSet.count() === 99999)
    }
    it("price data Dataset[PriceStr]") {
      val ss = PriceDataReader.dataSetStr.sparkSession
      import ss.implicits._
      PriceDataReader.dataSetStr.withColumn("real_date", org.apache.spark.sql.functions.date_format($"date", "YYYY-mm-dd"))
    }
    it("priceDataSet should have A records") {
      val A = PriceDataReader.getData("A")
      println(A.count())
      assert(PriceDataReader.priceDataSet.count() === 99999)
    }
  }
}
