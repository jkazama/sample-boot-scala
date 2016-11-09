package sample.util

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import sample.ValidationException

@RunWith(classOf[JUnitRunner])
class ValidatorSpec extends WordSpec with Matchers {
  
  "審査ユーティリティ検証" should {
    "単純ケース" in {
      try {
        Validator.validate(
          _.verify(true, "success")
           .verify(false, "failure")
        )
        fail()
      } catch {
        case e: ValidationException => e.message should be ("failure")
      }
    }
    "フィールドチェックケース" in {
      try {
        Validator.validate(
          _.checkField(true, "a", "success")
           .checkField(false, "b", "failureB")
           .checkField(false, "c", "failureC")
           .checkField(false, "d", "failureD")
        )
        fail()
      } catch {
        case e: ValidationException =>
          e.list.length should be (3)
          e.message should be ("failureB")
      }
    }
  }
}