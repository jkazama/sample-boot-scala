package sample.model.asset

import java.time.LocalDate
import scala.util.{ Try, Success, Failure }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import sample.ActionStatusType
import sample.UnitSpecSupport
import sample.model.DataFixtures._
import sample.model.account.AccountStatusType
import sample.model.asset._
import scalikejdbc._
import sample.model.DomainErrorKeys
import sample.ErrorKeys

//low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
@RunWith(classOf[JUnitRunner])
class CashInOutSpec extends UnitSpecSupport {
  val ccy = "JPY"
  val accId = "test"

  behavior of "振込入出金依頼"
  
  override def postBefore = {implicit s: DBSession =>
    // 残高1000円の口座(test)を用意
    saveSelfFiAcc(Remarks.CashOut, ccy)
    saveAcc(accId, AccountStatusType.NORMAL)
    saveFiAcc(accId, Remarks.CashOut, ccy)
    saveCb(accId, businessDay.day, ccy, "1000")
  }
  
  it should "振込入出金を検索する" in { implicit session =>
    val baseDay = businessDay.day
    val basePlus1Day = businessDay.day(1)
    val basePlus2Day = businessDay.day(2)
    saveCio(businessDay, accId, "300", true)
    //low: ちゃんとやると大変なので最低限の検証
    CashInOut.find(findParam(baseDay, basePlus1Day)).size should be (1)
    CashInOut.find(findParam(baseDay, basePlus1Day, ActionStatusType.UNPROCESSED)).size should be (1)
    CashInOut.find(findParam(baseDay, basePlus1Day, ActionStatusType.PROCESSED)).size should be (0)
    CashInOut.find(findParam(basePlus1Day, basePlus2Day, ActionStatusType.UNPROCESSED)).size should be (0)
  }
  def findParam(fromDay: LocalDate, toDay: LocalDate, statusTypes: ActionStatusType*) =
    FindCashInOut(Some(ccy), statusTypes, fromDay, toDay)
  
  it should "振込出金依頼をする" in { implicit session =>
    val baseDay = businessDay.day
    val basePlus3Day = businessDay.day(3)
    // 超過の出金依頼 [例外]
    Try(CashInOut.withdraw(businessDay, RegCashOut(Some(accId), ccy, BigDecimal("1001")))) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (AssetErrorKeys.CashInOutWithdrawAmount)
    }
    // 0円出金の出金依頼 [例外]
    Try(CashInOut.withdraw(businessDay, RegCashOut(Some(accId), ccy, BigDecimal("0")))) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (DomainErrorKeys.AbsAmountZero)
    }
    // 通常の出金依頼
    val normal = CashInOut.load(
      CashInOut.withdraw(businessDay, RegCashOut(Some(accId), ccy, BigDecimal("300"))))
    normal.accountId should be (accId)
    normal.currency should be (ccy)
    normal.absAmount should be (BigDecimal("300"))
    normal.withdrawal should be (true)
    normal.requestDay should be (baseDay)
    normal.eventDay should be (baseDay)
    normal.valueDay should be (basePlus3Day)
    normal.targetFiCode should be (s"${Remarks.CashOut}-${ccy}")
    normal.targetFiAccountId should be (s"FI${accId}")
    normal.selfFiCode should be (s"${Remarks.CashOut}-${ccy}")
    normal.selfFiAccountId should be ("xxxxxx")
    normal.statusType should be (ActionStatusType.UNPROCESSED)
    normal.cashflowId should be (None)
    
    // 拘束額を考慮した出金依頼 [例外]
    Try(CashInOut.withdraw(businessDay, RegCashOut(Some(accId), ccy, BigDecimal("701")))) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (AssetErrorKeys.CashInOutWithdrawAmount)
    }
  }
  
  it should "振込出金依頼を取消する" in { implicit session =>
    // CF未発生の依頼を取消
    val normal = saveCio(businessDay, accId, "300", true)
    normal.cancel()
    CashInOut.load(normal.id).statusType should be (ActionStatusType.CANCELLED)
    
    // 発生日を迎えた場合は取消できない [例外]
    val today = saveCio(businessDay, accId, "300", true, Some(businessDay.day))
    Try(today.cancel()) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (AssetErrorKeys.CashInOutBeforeEqualsDay)
    }
  }
  
  it should "振込出金依頼を例外状態とする" in { implicit session =>
    // CF未発生の依頼を取消
    val normal = saveCio(businessDay, accId, "300", true)
    normal.error()
    CashInOut.load(normal.id).statusType should be (ActionStatusType.ERROR)
    
    // 処理済の時はエラーにできない [例外]
    val processed = saveCio(businessDay, accId, "300", true, Some(businessDay.day))
    processed.process()
    Try(CashInOut.load(processed.id).error()) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (ErrorKeys.ActionUnprocessing)
    }
  }
  
  it should "発生日を迎えた振込入出金をキャッシュフロー登録する" in { implicit session =>
    val baseDay = businessDay.day
    val basePlus3Day = businessDay.day(3)
    // 発生日未到来の処理 [例外]
    val future = saveCio(businessDay, accId, "300", true)
    Try(future.process()) match {
      case Success(v) => fail()
      case Failure(e) => e.getMessage should be (AssetErrorKeys.CashInOutAfterEqualsDay)
    }
    // 発生日到来処理
    val normal = saveCio(businessDay, accId, "300", true, Some(baseDay))
    val processed = CashInOut.load(normal.process())
    processed.statusType should be (ActionStatusType.PROCESSED)
    processed.cashflowId.isDefined should be (true)
    val cf = Cashflow.load(processed.cashflowId.get)
    cf.accountId should be (accId)
    cf.currency should be (ccy)
    cf.amount should be (BigDecimal("-300"))
    cf.cashflowType should be (CashflowType.CashOut)
    cf.remark should be (Remarks.CashOut)
    cf.eventDay should be (baseDay)
    cf.valueDay should be (baseDay)
    cf.statusType should be (ActionStatusType.PROCESSED)
  }

}