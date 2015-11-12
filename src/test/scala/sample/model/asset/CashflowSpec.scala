package sample.model.asset

import java.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import sample.UnitSpecSupport
import sample.model.DataFixtures
import scala.util.{Try, Success, Failure}
import sample.ActionStatusType
import scala.math.BigDecimal

//low: 簡易な正常系検証が中心。依存するCashBalanceの単体検証パスを前提。
@RunWith(classOf[JUnitRunner])
class CashflowSpec extends UnitSpecSupport {
  
  behavior of "キャッシュフロー"
  
  it should "キャッシュフローを登録する" in { implicit session =>
    val baseDay = businessDay.day
    val baseMinus1Day = businessDay.day(-1)
    val basePlus1Day = businessDay.day(1)
    println(basePlus1Day)
    // 過去日付の受渡でキャッシュフロー発生 [例外]
    Try(Cashflow.register(
        RegCashflow("test1", "JPY", BigDecimal("1000"), CashflowType.CashIn, "cashIn", None, baseMinus1Day))) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be ("error.Cashflow.beforeEqualsDay")
    }
    // 翌日受渡でキャッシュフロー発生
    val cf = Cashflow.load(Cashflow.register(
      RegCashflow("test1", "JPY", BigDecimal("1000"), CashflowType.CashIn, "cashIn", None, basePlus1Day)))
    cf.amount should be (BigDecimal(1000))
    cf.statusType should be (ActionStatusType.UNPROCESSED)
    cf.eventDay should be (baseDay)
    cf.valueDay should be (basePlus1Day)
  }
  
  it should "未実現キャッシュフローを実現する" in { implicit session =>
    import DataFixtures._
    val baseDay = businessDay.day
    val baseMinus1Day = businessDay.day(-1)
    val baseMinus2Day = businessDay.day(-2)
    val basePlus1Day = businessDay.day(1)
    CashBalance.getOrNew("test1", "JPY")
    
    // 未到来の受渡日 [例外]
    Try(saveCf("test1", "1000", baseDay, basePlus1Day).realize()) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be ("error.Cashflow.realizeDay")
    }
    
    // キャッシュフローの残高反映検証。  0 + 1000 = 1000
    val cfNormal = saveCf("test1", "1000", baseMinus1Day, baseDay)
    cfNormal.statusType should be (ActionStatusType.UNPROCESSED)
    CashBalance.getOrNew("test1", "JPY").amount should be (BigDecimal("0"))
    
    val cfNormalId = cfNormal.realize()
    Cashflow.load(cfNormalId).statusType should be (ActionStatusType.PROCESSED)
    CashBalance.getOrNew("test1", "JPY").amount should be (BigDecimal("1000"))
    
    // 処理済キャッシュフローの再実現 [例外]
    Try(Cashflow.load(cfNormalId).realize()) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be ("error.ActionStatusType.unprocessing")
    }
    
    // 過日キャッシュフローの残高反映検証。 1000 + 2000 = 3000
    val cfPast = saveCf("test1", "2000", baseMinus2Day, baseMinus1Day)
    Cashflow.load(cfPast.realize()).statusType should be (ActionStatusType.PROCESSED)
    CashBalance.getOrNew("test1", "JPY").amount should be (BigDecimal("3000"))
  }
  
  it should "発生即実現のキャッシュフローを登録する" in { implicit session =>
    CashBalance.getOrNew("test1", "JPY")
    CashBalance.getOrNew("test1", "JPY").amount should be (BigDecimal(0))
    Cashflow.register(
      RegCashflow("test1", "JPY", BigDecimal("1000"), CashflowType.CashIn, "cashIn", None, businessDay.day))
    CashBalance.getOrNew("test1", "JPY").amount should be (BigDecimal(1000))
  }
}