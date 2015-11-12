package sample.model.asset

import java.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import sample.UnitSpecSupport
import sample.model.DataFixtures
import scala.math.BigDecimal

//low: 簡易な正常系検証のみ
@RunWith(classOf[JUnitRunner])
class CashBalanceSpec extends UnitSpecSupport {
  
  behavior of "現金残高"
  
  it should "現金残高を追加する" in { implicit session =>
    DataFixtures.saveCb("test1", businessDay.day, "USD", "10.02")
    
    // 10.02 + 11.51 = 21.53
    CashBalance.add("test1", "USD", BigDecimal("11.51"))
    CashBalance.getOrNew("test1", "USD").amount should be (BigDecimal("21.53"))
    
    // 21.53 + 11.516 = 33.04 (端数切捨確認)
    CashBalance.add("test1", "USD", BigDecimal("11.516"))
    CashBalance.getOrNew("test1", "USD").amount should be (BigDecimal("33.04"))
    
    // 33.04 - 41.51 = -8.47 (マイナス値/マイナス残許容)
    CashBalance.add("test1", "USD", BigDecimal("-41.51"))
    CashBalance.getOrNew("test1", "USD").amount should be (BigDecimal("-8.47")) 
  }
  
  it should "現金残高を取得する" in { implicit session =>
    val baseDay = businessDay.day
    val baseMinus1Day = businessDay.day(-1);
    DataFixtures.saveCb("test1", businessDay.day, "JPY", "1000")
    DataFixtures.saveCb("test2", businessDay.day, "JPY", "3000")
    
    val cbNormal = CashBalance.getOrNew("test1", "JPY")
    cbNormal.accountId should be ("test1")
    cbNormal.baseDay should be (baseDay)
    cbNormal.amount should be (BigDecimal("1000"))
    
    val cbRoll = CashBalance.getOrNew("test2", "JPY")
    cbRoll.accountId should be ("test2")
    cbRoll.baseDay should be (baseDay)
    cbRoll.amount should be (BigDecimal("3000"))
    
    val cbNew = CashBalance.getOrNew("test3", "JPY")
    cbNew.accountId should be ("test3")
    cbNew.baseDay should be (baseDay)
    cbNew.amount should be (BigDecimal(0))
  }
  
}