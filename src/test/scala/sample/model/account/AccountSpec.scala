package sample.model.account

import sample.UnitSpecSupport
import sample.model.DataFixtures

import scala.util.{Failure, Success, Try}

class AccountSpec extends UnitSpecSupport {

  behavior of "口座管理"

  it should "通常口座が取得できる" in { implicit session =>
    DataFixtures.saveAcc("normal", AccountStatusType.NORMAL)
    val acc = Account.loadActive("normal")

    acc.id should be ("normal")
    acc.statusType should be (AccountStatusType.NORMAL)
  }

  it should "退会時に例外が発生する" in { implicit session =>
    DataFixtures.saveAcc("withdrawal", AccountStatusType.WITHDRAWAL)
    Try(Account.loadActive("withdrawal")) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be ("error.Account.loadActive")
    }
  }
}
