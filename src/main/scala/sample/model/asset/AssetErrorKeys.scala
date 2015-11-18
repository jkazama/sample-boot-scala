package sample.model.asset

/** 資産の審査例外で用いるメッセージキー定数 */
trait AssetErrorKeys {
  /** 受渡日を迎えていないため実現できません */
  val CashflowRealizeDay = "error.Cashflow.realizeDay"
  /** 既に受渡日を迎えています */
  val CashflowBeforeEqualsDay = "error.Cashflow.beforeEqualsDay"

  /** 未到来の受渡日です */
  val CashInOutAfterEqualsDay = "error.CashInOut.afterEqualsDay"
  /** 既に発生日を迎えています */
  val CashInOutBeforeEqualsDay = "error.CashInOut.beforeEqualsDay"
  /** 出金可能額を超えています */
  val CashInOutWithdrawAmount = "error.CashInOut.withdrawAmount"
}
object AssetErrorKeys extends AssetErrorKeys