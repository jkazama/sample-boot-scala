package sample.util

import scala.math.BigDecimal.RoundingMode

import org.junit.runner.RunWith
import org.scalatest.{WordSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers

@RunWith(classOf[JUnitRunner])
class CalculatorSpec extends WordSpec with Matchers {

  "計算ユーティリティ検証" should {
    "四則演算" in {
      (Calculator("10.3", 2, RoundingMode.DOWN) + BigDecimal("10.2")).decimal should be (BigDecimal("20.50"))
      (Calculator("10.3", 2, RoundingMode.DOWN) - BigDecimal("10.2")).decimal should be (BigDecimal("0.10"))
      (Calculator("1.3", 2, RoundingMode.DOWN) * BigDecimal("1.2")).decimal should be (BigDecimal("1.56"))
      (Calculator("1", 2, RoundingMode.UP) / BigDecimal("3")).decimal should be (BigDecimal("0.34"))
    }
  }

}
