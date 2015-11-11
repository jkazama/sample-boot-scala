package sample.model.asset

import sample.context._
import com.fasterxml.jackson.annotation.JsonValue
import sample.context.EnumSealed
import sample.ActionStatusType
import java.time.LocalDateTime
import java.time.LocalDate
import sample.context.orm.SkinnyORMMapper
import scalikejdbc.jsr310.Implicits._
import scalikejdbc._
import sample.context.Dto
import sample.util.Validator
import sample._
import scala.util.{Try, Success, Failure}

/**
 * 入出金キャッシュフローを表現します。
 * キャッシュフローは振込/振替といったキャッシュフローアクションから生成される確定状態(依頼取消等の無い)の入出金情報です。
 * low: 概念を伝えるだけなので必要最低限の項目で表現しています。
 * low: 検索関連は主に経理確認や帳票等での利用を想定します
 */
case class Cashflow(
  /** ID */
  id: Long = 0,
  /** 口座ID */
  accountId: String,
  /** 通貨 */
  currency: String,
  /** 金額 */
  amount: BigDecimal,
  /** 入出金 */
  cashflowType: CashflowType,
  /** 摘要*/
  remark: String,
  /** 発生日 */
  eventDay: LocalDate,
  /** 発生日時 */
  eventDate: LocalDateTime,
  /** 受渡日 */
  valueDay: LocalDate,
  /** 処理種別 */
  statusType: ActionStatusType) extends Entity {
  
  /** キャッシュフローを実現(受渡)可能か判定します。 */
  def canRealize(implicit dh: DomainHelper): Boolean =
    dh.time.tp.afterEqualsDay(valueDay)

  /** キャッシュフローを処理済みにして残高へ反映します。 */
  def realize()(implicit s: DBSession, dh: DomainHelper): Long =
    Validator.validateTry(v => {
      v.verify(canRealize, "error.Cashflow.realizeDay")
      v.verify(statusType.isUnprocessing, "error.ActionStatusType.unprocessing")
    }) match {
      case Success(v) =>
        Cashflow.updateById(id).withAttributes(
          'statusType -> ActionStatusType.PROCESSED.value)
        CashBalance.add(accountId, currency, amount)
        id
      case Failure(e) => throw e
    }
  
  /**
   * キャッシュフローをエラー状態にします。
   * <p>処理中に失敗した際に呼び出してください。
   * low: 実際はエラー事由などを引数に取って保持する
   */
  def error()(implicit s: DBSession, dh: DomainHelper): Unit =
    Validator.validateTry(v =>
      v.verify(statusType.isUnprocessed, "error.ActionStatusType.unprocessing")
    ) match {
      case Success(v) =>
        Cashflow.updateById(id).withAttributes(
          'statusType -> ActionStatusType.ERROR.value)
      case Failure(e) => throw e
    }
}

object Cashflow extends CashflowMapper {
  
  /** キャッシュフローを取得します。 */
  def load(id: Long)(implicit s: DBSession): Cashflow =
    findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
  
  /** 指定受渡日時点で未実現のキャッシュフロー一覧を検索します。 */
  def findUnrealize(valueDay: LocalDate)(implicit s: DBSession): List[Cashflow] =
    withAlias(m =>
      findAllBy(
        sqls
          .le(m.valueDay, valueDay)
          .in(m.statusType, ActionStatusType.unprocessingTypes),
        Seq(m.id)))

  /** 指定受渡日で実現対象となるキャッシュフロー一覧を検索します。 */
  def findDoRealize(valueDay: LocalDate)(implicit s: DBSession): List[Cashflow] =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.valueDay, valueDay)
          .in(m.statusType, ActionStatusType.unprocessedTypes),
        Seq(m.id)))

  /**
   * キャッシュフローを登録します。
   * 受渡日を迎えていた時はそのまま残高へ反映します。
   */
  def register(p: RegCashflow)(implicit s: DBSession, dh: DomainHelper): Long =
    Validator.validateTry(v =>
      v.checkField(dh.time.tp.beforeEqualsDay(p.valueDay), "valueDay", "error.Cashflow.beforeEqualsDay")
    ) match {
      case Success(v) =>
        create(p).map(cf => if (cf.canRealize) cf.realize() else cf.id).get
      case Failure(e) => throw e
    }
  private def create(p: RegCashflow)(implicit s: DBSession, dh: DomainHelper): Option[Cashflow] =
    findById(createWithAttributes(
      'accountId -> p.accountId,
      'currency -> p.currency,
      'amount -> p.amount,
      'cashflowType -> p.cashflowType.value,
      'remark -> p.remark,
      'eventDay -> p.eventDay.getOrElse(dh.time.day),
      'eventDate -> dh.time.date,
      'valueDay -> p.valueDay,
      'statusType -> ActionStatusType.UNPROCESSED.value))

}
  
trait CashflowMapper extends SkinnyORMMapper[Cashflow] {
  override def extract(rs: WrappedResultSet, n: ResultName[Cashflow]) =
    Cashflow(
      accountId = rs.string(n.accountId),
      currency = rs.string(n.currency),
      amount = rs.bigDecimal(n.amount),
      cashflowType = CashflowType.withName(rs.string(n.cashflowType)),
      remark = rs.string(n.remark),
      eventDay = rs.localDate(n.eventDay),
      eventDate = rs.localDateTime(n.eventDate),
      valueDay = rs.localDate(n.valueDay),
      statusType = ActionStatusType.withName(rs.string(n.statusType)))
}
  
/** キャッシュフロー種別。 low: 各社固有です。摘要含めラベルはなるべくmessages.propertiesへ切り出し */
sealed trait CashflowType extends EnumSealed {
  @JsonValue def value: String = this.toString()
}
object CashflowType extends Enums[CashflowType] {
  /** 振込入金 */
  case object CashIn extends CashflowType
  /** 振込出金 */
  case object CashOut extends CashflowType
  /** 振替入金 */
  case object CashTransferIn extends CashflowType
  /** 振替出金 */
  case object CashTransferOut extends CashflowType
  
  override def values = List(CashIn, CashOut, CashTransferIn, CashTransferOut)
}

/** 入出金キャッシュフローの登録パラメタ。 (発生日未指定時は営業日を設定) */
case class RegCashflow(
  accountId: String, currency: String, amount: BigDecimal, cashflowType: CashflowType,
  remark: String, eventDay: Option[LocalDate] = None, valueDay: LocalDate) extends Dto
