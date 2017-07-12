package com.treasure.util

import com.typesafe.config.ConfigFactory

/**
  * Created by gcrowell on 2017-07-11.
  */
object Config {
  val config = ConfigFactory.load()

  val dataRootPath = config.getString("treasure.data.root")
  val priceRootPath = config.getString("treasure.data.price")
  val statementRootPath = config.getString("treasure.data.statement")
}
