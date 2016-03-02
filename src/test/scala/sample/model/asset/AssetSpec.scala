package sample.model.asset

import java.time.LocalDate

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sample.UnitSpecSupport
import sample.model.DataFixtures._
import sample.model.account.AccountStatusType

//low: 簡易な正常系検証のみ
@RunWith(classOf[JUnitRunner])
class AssetSpec extends UnitSpecSupport {
  
  behavior of "資産"
  
  it should "振込出金可能か判定する" in { implicit session =>
    saveAcc("test", AccountStatusType.Normal)
    saveCb("test", LocalDate.of(2014, 11, 18), "JPY", "10000")
    saveCf("test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20))
    saveCf("test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21))
    saveCio(businessDay, "test", "8000", true)
    
    Asset("test").canWithdraw("JPY", BigDecimal("1000"), LocalDate.of(2014, 11, 21)) should be (true)
    Asset("test").canWithdraw("JPY", BigDecimal("1001"), LocalDate.of(2014, 11, 21)) should be (false)
  }
}