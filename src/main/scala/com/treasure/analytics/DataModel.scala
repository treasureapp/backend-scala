package com.treasure.analytics

/**
  * Created by gcrowell on 2017-07-17.
  */
object DataModel {

}

trait TimeSpan {
  def start: Long
  def end: Long
}


trait SubjectStateProcess[A] extends Seq[A] {
  def subjectId: Long
  def getStateValue(datePoint: Long): A
}

