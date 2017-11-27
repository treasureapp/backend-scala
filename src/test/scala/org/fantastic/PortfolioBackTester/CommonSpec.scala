
package org.fantastic.PortfolioBackTester

import java.util.Calendar

import org.scalatest.{FunSpec, Matchers}

/**
  * Created by gcrowell on 2017-05-28.
  */
class CommonSpec extends FunSpec with Matchers {
  describe("Calendar conversions") {
    it("Not defined at invalid Int") {
      import Common._
      val int = 20170100
      val calendar: Calendar = int
    }

    it("Int to Calendar") {
      import Common._
      val int = 20170101

      // force implicit conversion by sending Int to Calendar function
      val calendarFunction = (calendar: Calendar) => calendar
      val intAsCalendar: Calendar = calendarFunction(int)
      val expected = Calendar.getInstance()
      expected.set(Calendar.YEAR, 2017)
      expected.set(Calendar.MONTH, Calendar.JANUARY)
      expected.set(Calendar.DAY_OF_MONTH, 1)

      intAsCalendar.get(Calendar.YEAR) should be(expected.get(Calendar.YEAR))
      intAsCalendar.get(Calendar.MONTH) should be(expected.get(Calendar.MONTH))
      intAsCalendar.get(Calendar.DAY_OF_MONTH) should be(expected.get(Calendar.DAY_OF_MONTH))
    }
    // TODO test Calendar to Int
    it("Calendar to Int") {
      import Common._
      val int = 20170101
      val calendar: Calendar = int
      val expected = Calendar.getInstance()
      expected.set(Calendar.YEAR, 2017)
      expected.set(Calendar.MONTH, Calendar.JANUARY)
      expected.set(Calendar.DAY_OF_MONTH, 1)

      calendar.get(Calendar.YEAR) should be(expected.get(Calendar.YEAR))
      calendar.get(Calendar.MONTH) should be(expected.get(Calendar.MONTH))
      calendar.get(Calendar.DAY_OF_MONTH) should be(expected.get(Calendar.DAY_OF_MONTH))
    }
  }
}
