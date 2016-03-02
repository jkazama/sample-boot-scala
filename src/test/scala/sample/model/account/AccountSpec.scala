package sample.model.account

import scala.util.{ Failure, Success, Try }

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sample._
import sample.model.DataFixtures

@RunWith(classOf[JUnitRunner])
class AccountSpec extends UnitSpecSupport {

  behavior of "口座管理"
  
  it should "通常口座が取得できる" in { implicit session =>
    DataFixtures.saveAcc("normal", AccountStatusType.Normal)
    val acc = Account.loadActive("normal")

    acc.id should be ("normal")
    acc.statusType should be (AccountStatusType.Normal)
  }

  it should "退会時に例外が発生する" in { implicit session =>
    DataFixtures.saveAcc("withdrawal", AccountStatusType.Withdrawal)
    Try(Account.loadActive("withdrawal")) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be ("error.Account.loadActive")
    }
  }
  
  it should "口座を登録する" in { implicit session =>
    val p = RegAccount("regsample", "sample user", "sample@example.com", "passwd")
    val id = Account.register(encoder, p)
    val acc = Account.findById(id)
    acc.isDefined should be (true)
    acc.get.id should be ("regsample")
    acc.get.name should be ("sample user")
    acc.get.mail should be ("sample@example.com")
    val login = Login.findById(id)
    login.isDefined should be (true)
    login.get.id should be ("regsample")
    login.get.loginId should be ("regsample")
    encoder.matches("passwd", login.get.password) should be (true)
  }
  
  it should "口座を変更する" in { implicit session =>
    DataFixtures.saveAcc("normal", AccountStatusType.Normal)
    val p = ChgAccount("sample user", "sample@example.com")
    Account.change("normal", p)
    val acc = Account.findById("normal")
    acc.isDefined should be (true)
    acc.get.name should be ("sample user")
    acc.get.mail should be ("sample@example.com")
  }
}
