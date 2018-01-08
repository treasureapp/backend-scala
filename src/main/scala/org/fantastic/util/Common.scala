
package org.fantastic.util

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by Dust on 5/28/2017.
  */
object Converters {

  val dateIdFormat = new SimpleDateFormat("yyyyMMdd")

  implicit def toInt(x: Calendar): Int = {
    dateIdFormat.format(x.getTime()).toInt
  }

  implicit def toCalendar(dateId: Int): Calendar = {
    val calendarOut = Calendar.getInstance()
    calendarOut.setTime(dateIdFormat.parse(dateId.toString))
    calendarOut
  }


}

/**
  * see http://docs.scala-lang.org/style/naming-conventions.html#constants-values-variable-and-methods
  */
object Constants {
  val EpochDateId: Int = 19900827 // pearl jam 10
}


object DateUtils {

  import Converters._

  def getMostRecentCompleteTradeDateId(now: Calendar = Calendar.getInstance()): Int = {
    val offsetDayCount = getOffsetDayCount(now)
    now.add(Calendar.DAY_OF_MONTH, offsetDayCount)
    now
  }

  def getOffsetDayCount(today: Calendar): Int = {
    val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
    val todayHourOfDay = today.get(Calendar.HOUR_OF_DAY)
    todayDayOfWeek match {
      case Calendar.SATURDAY => -1
      case Calendar.SUNDAY => -2
      case Calendar.MONDAY => todayHourOfDay match {
        case _ => if (todayHourOfDay < 13) -3 else 0
      }
      // if todayDayOfWeek is T, W, H, or F and markets open (ie <13) then yesterday is expected
      case _ => if (todayHourOfDay < 13) -1 else 0
    }
  }
}

/**
  * generic placeholder for anything that can be held in a Portfolio (stock, option, bond, commodity, cash, ...)
  * if the Asset is a
  * Stock then ticker is the symbol
  * MSFT, FB
  * Cash then ticker is the currency code
  * CAD, USD, ...
  * ...
  */
trait Asset {
  def ticker: String
}

/**
  * Financial data type description
  * https://www.scala-lang.org/api/2.12.2/scala/Enumeration.html
  */
object DataType extends Enumeration {
  type DataType = Value
  val StockPrice, FinancialStatement = Value
}

import DataType._

/**
  * meta data for a time series data set
  *
  */
trait MetaTimeSeries extends Asset {

  import Constants._
  import java.util.Calendar
  import Converters._

  override def ticker: String

  def dataType: DataType

  def startDate: Option[Calendar] = Some(EpochDateId)

  def endDate: Option[Calendar] = None
}

class DataRequest(val ticker: String, val dataType: DataType, override val startDate: Calendar, override val endDate: Calendar) extends MetaTimeSeries {}

class TimeSeries(val ticker: String, val dataType: DataType, override val startDate: Calendar, override val endDate: Calendar, timeSeriesData: Seq[(Int, Double)]) extends Seq[(Int, Double)] with MetaTimeSeries {


  override def iterator: Iterator[(Int, Double)] = timeSeriesData.iterator

  override def length: Int = timeSeriesData.length

  override def apply(idx: Int): (Int, Double) = timeSeriesData(idx)
}

object TimeSeries {

}


