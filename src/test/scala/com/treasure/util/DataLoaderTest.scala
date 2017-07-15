package com.treasure.util

import org.scalatest.FunSuite

/**
  * Created by gcrowell on 2017-07-15.
  */
class DataLoaderTest extends FunSuite {

  test("testGetData") {
    val data = DataLoader.getData
    data.foreach(println)
    val flatData = data.flatten
    flatData.foreach(println)

    println(data)

  }


  test("testToPricePoint") {
    val data = DataLoader.getData
    val seqPricePoint : Seq[PricePoint] = DataLoader.toPricePoint(data)
    data.foreach(println)
    val flatData = data.flatten
    flatData.foreach(println)

    println(data)

  }



}
