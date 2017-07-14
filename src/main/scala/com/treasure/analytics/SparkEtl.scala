package com.treasure.analytics

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.DoubleType

/**
  * Created by gcrowell on 2017-07-12.
  */
object SparkEtl extends LazyLogging {

  val toDouble: UserDefinedFunction = {
    udf((input: String) => try {
      input.toDouble
    } catch {
      case e: Exception => 0.0
    }, DoubleType)
  }


}


object DemoEtl {


}