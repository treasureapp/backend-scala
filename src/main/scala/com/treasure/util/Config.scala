package com.treasure.util

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory

/**
  * Created by gcrowell on 2017-07-11.
  */
object Config extends LazyLogging {

  logger.debug("loading application.conf...")

  val config = ConfigFactory.load()

  val dataRootPath = config.getString("treasure.data.root")
  val priceRootPath = config.getString("treasure.data.price")
  val statementRootPath = config.getString("treasure.data.statement")

  logger.debug(s"${config.atKey("treasure")}")
}
