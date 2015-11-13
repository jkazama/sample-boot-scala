package sample.model.account

import scala.util.{ Failure, Success, Try }

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sample._
import sample.model.DataFixtures

@RunWith(classOf[JUnitRunner])
class FiAccountSpec extends UnitSpecSupport {

  behavior of "金融機関口座"

  it should "口座を取得する" in { implicit session =>
    DataFixtures.saveFiAcc("sample", "cate1", "JPY")

    Try(FiAccount.load("sample", "cate1", "USD")) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (ErrorKeys.EntityNotFound)
    }

    val acc = FiAccount.load("sample", "cate1", "JPY")
    acc.accountId should be ("sample")
    acc.category should be ("cate1")
    acc.currency should be ("JPY")
  }

}
