package com.treasure.data

import java.util.Calendar

/**
  * Created by gcrowell on 2017-06-23.
  */
object Constants {
  private val _epoch = Calendar.getInstance()
  _epoch.set(1991, 10, 1)

  def epoch: Calendar = {
    _epoch
  }
}
