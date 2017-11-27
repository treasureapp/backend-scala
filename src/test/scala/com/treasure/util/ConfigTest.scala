package com.treasure.util

import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.matchers._

import scala.reflect.io.Path

/**
  * Created by gcrowell on 2017-07-13.
  */
trait CustomPathMatcher {

  class DirectoryMatcher extends BeMatcher[scala.reflect.io.Path] {
    override def apply(left: Path): MatchResult = MatchResult(
      left.isDirectory,
      left.toCanonical + " is a Directory",
      left.toCanonical + " is not a Directory"
    )
  }
  class FileMatcher extends BeMatcher[scala.reflect.io.Path] {
    override def apply(left: Path): MatchResult = MatchResult(
      left.canRead && left.isFile,
      left.toCanonical + " is a readable File",
      left.toCanonical + " is not a readable File"
    )
  }
  val directory = new DirectoryMatcher
  val file = new FileMatcher
}
object CustomPathMatcher extends CustomPathMatcher

class ConfigTest extends FunSpec with BeforeAndAfter with Matchers with CustomPathMatcher {

  val config = ConfigFactory.load()

  describe("application.conf") {
    it("should load path strings from application.conf") {
      val dataRootPathStr = config.getString("treasure.data.root")
      dataRootPathStr.length should be > 0
      val priceRootPathStr = config.getString("treasure.data.price_file")
      priceRootPathStr.length should be > 0
    }
    it("should contain valid (data) paths") {
      val dataRootPathStr = config.getString("treasure.data.root")
      val dataRootPath = scala.reflect.io.Path(dataRootPathStr)
      dataRootPath shouldBe directory
      val priceRootPathStr = config.getString("treasure.data.price_file")
      val pricePath = scala.reflect.io.Path(priceRootPathStr)
      pricePath shouldBe file
    }
  }
}
