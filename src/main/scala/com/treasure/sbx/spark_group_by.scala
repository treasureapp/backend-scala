package com.treasure.sbx

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by gcrowell on 2017-07-15.
  */
object spark_group_by {
  def groupByCount(ss: SparkSession, priceDs: Dataset[PricePoint]): Long = {
    import ss.implicits._
    priceDs.map((x:PricePoint)=> (x.subjectId)).distinct().count()
  }
}
