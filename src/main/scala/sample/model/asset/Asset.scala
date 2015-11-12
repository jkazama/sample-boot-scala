package sample.model.asset

import java.time.LocalDate
import scalikejdbc.jsr310.Implicits._
import scalikejdbc._
import sample.context._
import sample.util.Calculator

/**
 * 口座の資産概念を表現します。
 * asset配下のEntityを横断的に取り扱います。
 * low: 実際の開発では多通貨や執行中/拘束中のキャッシュフローアクションに対する考慮で、サービスによってはかなり複雑になります。
 */
case class Asset(/** 口座ID */ id: String) {
  /**
   * 振込出金可能か判定します。
   * <p>0 &lt;= 口座残高 + 未実現キャッシュフロー - (出金依頼拘束額 + 出金依頼額) 
   * low: 判定のみなのでscale指定は省略。余力金額を返す時はきちんと指定する
   */
  def canWithdraw(currency: String, absAmount: BigDecimal, valueDay: LocalDate)(implicit s: DBSession, dh: DomainHelper): Boolean = {
    0 <=
      (calcUnprocessedCio(
        calcUnrealizeCf(calcCashBalance(currency), currency, valueDay), currency)
        - absAmount
      ).decimal.signum
  }
  private def calcCashBalance(currency: String)(implicit s: DBSession, dh: DomainHelper): Calculator =
    Calculator(CashBalance.getOrNew(id, currency).amount)
  private def calcUnrealizeCf(base: Calculator, currency: String, valueDay: LocalDate)(implicit s: DBSession, dh: DomainHelper): Calculator =
    Cashflow.findUnrealize(id, currency, valueDay).foldLeft(base)(
        (calc, cf) => calc + cf.amount)
  private def calcUnprocessedCio(base: Calculator, currency: String)(implicit s: DBSession, dh: DomainHelper): Calculator =
    CashInOut.findUnprocessed(id, currency, true).foldLeft(base)(
        (calc, cio) => calc - cio.absAmount)

}