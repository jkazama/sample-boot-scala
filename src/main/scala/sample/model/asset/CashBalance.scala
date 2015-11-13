package sample.model.asset

import java.time.{LocalDate, LocalDateTime}
import java.util.Currency

import scala.math.BigDecimal.RoundingMode

import sample.context._
import sample.context.orm.SkinnyORMMapper
import sample.util.Calculator
import scalikejdbc.jsr310._
import scalikejdbc._

/**
 * 口座残高を表現します。
 */
case class CashBalance(
  /** ID */
  id: Long = 0,
  /** 口座ID */
  accountId: String,
  /** 基準日 */
  baseDay: LocalDate,
  /** 通貨 */
  currency: String,
  /** 金額 */
  amount: BigDecimal,
  /** 更新日時 */
  updateDate: LocalDateTime) extends Entity

object CashBalance extends SkinnyORMMapper[CashBalance] {
  override def extract(rs: WrappedResultSet, rn: ResultName[CashBalance]): CashBalance = autoConstruct(rs, rn)
  
  /**
   * 残高へ指定した金額を反映します。
   * low ここではCurrencyを使っていますが、実際の通貨桁数や端数処理定義はDBや設定ファイル等で管理されます。
   */
  def add(accountId: String, currency: String, amount: BigDecimal)(implicit s: DBSession, dh: DomainHelper): Unit =
    Option(getOrNew(accountId, currency)).map(cb =>
      updateById(cb.id).withAttributes(
        'amount ->
          (Calculator(
              cb.amount,
              Currency.getInstance(currency).getDefaultFractionDigits,
              RoundingMode.DOWN)
            + amount).decimal
      ))
  
  /**
   * 指定口座の残高を取得します。(存在しない時は繰越保存後に取得します)
   * low: 複数通貨の適切な考慮や細かい審査は本筋でないので割愛。
   */
  def getOrNew(accountId: String, currency: String)(implicit s: DBSession, dh: DomainHelper): CashBalance =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.accountId, accountId)
          .and.eq(m.currency, currency)
          .and.eq(m.baseDay, dh.time.day),
        Seq(m.baseDay.desc))
    ).headOption.getOrElse(create(accountId, currency))
  private def create(accountId: String, currency: String)(implicit s: DBSession, dh: DomainHelper): CashBalance =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.accountId, accountId)
          .and.eq(m.currency, currency),
        Seq(m.baseDay.desc))
    ).headOption match {
      case Some(prev) => // 繰越
        findById(persist(accountId, currency, prev.amount)).get
      case None =>
        findById(persist(accountId, currency, BigDecimal(0))).get
    }
  private def persist(accountId: String, currency: String, amount: BigDecimal)(implicit s: DBSession, dh: DomainHelper): Long =
    createWithAttributes(
      'accountId -> accountId,
      'baseDay -> dh.time.day,
      'currency -> currency,
      'amount -> amount,
      'updateDate -> dh.time.date)
 }
