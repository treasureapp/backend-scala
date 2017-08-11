package com.treasure.analytics

import org.joda.time.format.DateTimeFormat
import spire.math.Interval

/**
  * Created by gcrowell on 2017-07-17.
  */

/**
  * meta data field used by other types
  * @param label
  */
abstract class Label(val label: String)

/**
  * Unix datetime.
  * @param millis number of seconds since Unix epoch (Jan 1, 1970)
  * @see https://en.wikipedia.org/wiki/Unix_time
  */
abstract class Date(val millis: Long) {
  override def toString: String = {
    DateTimeFormat.forPattern("YYYY-MM-dd").print(millis)
  }
}


trait Event {
  def label: Label
  def date: Date

  override def toString = s"Event(label=$label, date=${date.toString})"
}

trait TimeGranularity

trait State {
  def label: Label

  def value: AnyVal

  def timeSpan: Interval[Date]
}

trait Attribute {
  def label: Label

  def value: AnyVal
}

trait Subject {
  def attributes: Seq[Attribute]

  def processes: Seq[Process]

  def events: Seq[Event]
}


trait SubjectStateProcess[A] extends Seq[A] {
  def subjectId: Long

  def getStateValue(datePoint: Long): A
}

