package controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import github.PairParser

class PairParsingSpec extends Specification {
  "The pair parser" should {
    "extract a map" in {
      val map = PairParser.parse("a=b&c=d")
      map("a") must be equalTo("b")
    }
  }
}
