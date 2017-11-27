package com.treasure.util

import org.scalatest._
import org.scalatest.matchers._

import scala.reflect.io.Path

/**
  * Created by gcrowell on 2017-07-13.
  */
trait CustomMatcher {

  class DirectoryMatcher extends BeMatcher[scala.reflect.io.Path] {
    override def apply(left: Path): MatchResult = MatchResult(
      left.isDirectory,
      left.toCanonical + " is a Directory",
      left.toCanonical + " is not a Directory"
    )
  }
  val directory = new DirectoryMatcher
}
object CustomMatcher extends CustomMatcher

class ConfigTest extends FunSpec with Matchers with CustomMatcher {
  describe("A application.conf") {
    it("should load strings from application.conf") {
      val dataRootPathStr = Config.dataRootPath
      dataRootPathStr.length should be > 0
      val priceRootPathStr = Config.priceRootPath
      priceRootPathStr.length should be > 0
      val statementRootPathStr = Config.statementRootPath
      statementRootPathStr.length should be > 0
    }
    it("should contain paths to directories") {
      val dataRoot = scala.reflect.io.Path(Config.dataRootPath)
      dataRoot shouldBe directory
      val priceRoot = scala.reflect.io.Path(Config.priceRootPath)
      priceRoot shouldBe directory
      val statementRoot = scala.reflect.io.Path(Config.statementRootPath)
      statementRoot shouldBe directory
    }
  }
}
