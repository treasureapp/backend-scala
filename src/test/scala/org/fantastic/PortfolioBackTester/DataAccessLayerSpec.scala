
package org.fantastic.PortfolioBackTester

import java.text.SimpleDateFormat
import java.util.Calendar

import org.scalatest.{FunSpec, Matchers}

/**
  * Created by gcrowell on 2017-05-28.
  */
class DataAccessLayerSpec extends FunSpec with Matchers {
  describe("getOffsetDayCount") {
    it("should offset with friday") {
      val priceSync = PriceSync
      val friday = Calendar.getInstance() //sets sunday as Calendar type
      friday.set(2017, 4, 26)
      //sets sunday as actual date
      val output = priceSync.getOffsetDayCount(friday) //
      output should be(0)
    }

    it("should offset saturday with friday") {
      val priceSync = PriceSync
      //val today = Calendar.SATURDAY //returns 7
      val saturday = Calendar.getInstance() //sets sunday as Calendar type
      saturday.set(2017, 4, 27)
      //sets sunday as actual date
      val friday = Calendar.getInstance() //sets friday as Calendar type
      friday.set(2017, 4, 26)
      //sets friday as actual date
      val output = priceSync.getOffsetDayCount(saturday) //
      output should be(-1)
    }

    it("should offset sunday with friday") {
      val priceSync = PriceSync
      //val today = Calendar.SATURDAY //returns 7
      val sunday = Calendar.getInstance() //sets saturday as Calendar type
      sunday.set(2017, 4, 28)
      //sets saturday as actual date
      val friday = Calendar.getInstance() //sets friday as Calendar type
      friday.set(2017, 4, 26)
      //sets friday as actual date
      val output = priceSync.getOffsetDayCount(sunday) //
      output should be(-2)
    }

    it("should offset Monday before 1:00pm PST with friday") {
      val priceSync = PriceSync
      //val today = Calendar.SATURDAY //returns 7
      val monday = Calendar.getInstance() //sets monday as Calendar type
      monday.set(2017, 4, 29, 12, 0)
      //sets monday as actual datetime
      val friday = Calendar.getInstance() //sets friday as Calendar type
      friday.set(2017, 4, 26)
      //sets friday as actual date
      val output = priceSync.getOffsetDayCount(monday) //
      output should be(-3)
    }

    it("should not offset Monday after 1:00pm PST with friday") {
      val priceSync = PriceSync
      //val today = Calendar.SATURDAY //returns 7
      val monday = Calendar.getInstance() //sets monday as Calendar type
      monday.set(2017, 4, 29, 15, 0)
      //sets monday as actual datetime
      val friday = Calendar.getInstance() //sets friday as Calendar type
      friday.set(2017, 4, 26)
      //sets friday as actual date
      val output = priceSync.getOffsetDayCount(monday) //
      output should be(0)
    }

    it("should Friday's date if Monday after 1:00pm PST") {
      val priceSync = PriceSync
      val monday = Calendar.getInstance() //sets monday as Calendar type
      monday.set(2017, 4, 29, 12, 0)
      //sets monday as actual datetime
      val friday = Calendar.getInstance() //sets friday as Calendar type
      friday.set(2017, 4, 26)
      //sets friday as actual date
      val output = priceSync.getExpectedMostRecentDate(monday)
      val s = new SimpleDateFormat("yyyyMMdd")
      output should be(s.format(friday.getTime()).toInt)
    }

    it("should Tuesday's date if Wednesday before 1:00pm PST") {
      val priceSync = PriceSync
      val wednesday = Calendar.getInstance()
      wednesday.set(2017, 4, 31, 9, 0)
      // Wednesday May, 31 10:00am PST
      val tuesday = Calendar.getInstance()
      tuesday.set(2017, 4, 30)
      // Tuesday May, 30
      val output = priceSync.getExpectedMostRecentDate(wednesday)
      val s = new SimpleDateFormat("yyyyMMdd")
      output should be(s.format(tuesday.getTime()).toInt)
    }
  }
}


class DataAccessLayerSpecOutdated extends FunSpec with Matchers {
  describe("getOutdated") {
    it("should not have any null dates") {
      val priceSync = PriceSync
      val updatePriceTaskSpecSeq: Seq[UpdatePriceTaskSpec] = priceSync.getOutDated()
      val row = updatePriceTaskSpecSeq(0)
      val fromDate = row.fromDate
      println(s"fromDate.toString() = ${fromDate.toString()}")

    }
  }
}

//class DataAccessLayerSpecDownload extends FunSpec with Matchers {
//  describe("downloadData") {
//    val updatePriceTaskSpec = UpdatePriceTaskSpec("MSFT", Some(20150101))
//    val seqTuple = PriceSync.downloadData(updatePriceTaskSpec).filter((row) => row._2 < 20160000)
//    println(seqTuple.length)
//
//  }
//}