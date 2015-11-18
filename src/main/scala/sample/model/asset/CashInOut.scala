package sample.model.asset

import java.time.{LocalDate, LocalDateTime}
import scala.util.{ Success, Failure }
import sample._
import sample.context._
import sample.context.orm.SkinnyORMMapper
import sample.model.BusinessDayHandler
import sample.model.account.FiAccount
import sample.model.master.SelfFiAccount
import sample.util._
import scalikejdbc._
import scalikejdbc.jsr310.Implicits._
import sample.model.DomainErrorKeys

/**
 * 振込入出金依頼を表現するキャッシュフローアクション。
 * <p>相手方/自社方の金融機関情報は依頼後に変更される可能性があるため、依頼時点の状態を
 * 保持するために非正規化して情報を保持しています。
 * low: 相手方/自社方の金融機関情報は項目数が多いのでサンプル用に金融機関コードのみにしています。
 * 実際の開発ではそれぞれ複合クラス(FinantialInstitution)に束ねるアプローチを推奨します。
 */
case class CashInOut(
  /** ID(振込依頼No) */
  id: Long = 0,
  /** 口座ID */
  accountId: String,
  /** 通貨 */
  currency: String,
  /** 金額(絶対値) */
  absAmount: BigDecimal,
  /** 出金時はtrue */
  withdrawal: Boolean,
  /** 依頼日 */
  requestDay: LocalDate,
  /** 依頼日時 */
  requestDate: LocalDateTime,
  /** 発生日 */
  eventDay: LocalDate,
  /** 受渡日 */
  valueDay: LocalDate,
  /** 相手方金融機関コード */
  targetFiCode: String,
  /** 相手方金融機関口座ID */
  targetFiAccountId: String,
  /** 自社方金融機関コード */
  selfFiCode: String,
  /** 自社方金融機関口座ID */
  selfFiAccountId: String,
  /** 処理種別 */
  statusType: ActionStatusType,
  /** キャッシュフローID。処理済のケースでのみ設定されます。low: 実際は調整CFや消込CFの概念なども有 */
  cashflowId: Option[Long],
  /** 更新日時 */
  updateDate: LocalDateTime) extends Entity {
  
  /**
   * 依頼を処理します。
   * <p>依頼情報を処理済にしてキャッシュフローを生成します。
   */
  def process()(implicit s: DBSession, dh: DomainHelper): Long =
    Validator.validateTry(v =>
      v.verify(statusType.isUnprocessed, ErrorKeys.ActionUnprocessing)
       .verify(dh.time.tp.afterEqualsDay(eventDay), AssetErrorKeys.CashInOutAfterEqualsDay)
    ) match {
      case Success(v) =>
        CashInOut.updateById(id).withAttributes(
          'statusType -> ActionStatusType.PROCESSED.value,
          'cashflowId -> Some(Cashflow.register(regCf)))
        id
      case Failure(e) => throw e
    }
  //low: 摘要はとりあえずシンプルに。実際はCashInOutへ用途フィールドをもたせた方が良い(生成元メソッドに応じて摘要を変える)
  private def regCf: RegCashflow =
    if (withdrawal)
      RegCashflow(accountId, currency, -absAmount, CashflowType.CashOut,
        Remarks.CashOut, Some(eventDay), valueDay)
    else
      RegCashflow(accountId, currency, absAmount, CashflowType.CashIn,
        Remarks.CashIn, Some(eventDay), valueDay)

  /**
   * 依頼を取消します。
   * <p>"処理済みでない"かつ"発生日を迎えていない"必要があります。
   */
  def cancel()(implicit s: DBSession, dh: DomainHelper): Unit =
    Validator.validateTry(v =>
      v.verify(statusType.isUnprocessing, ErrorKeys.ActionUnprocessing)
       .verify(dh.time.tp.beforeDay(eventDay), AssetErrorKeys.CashInOutBeforeEqualsDay)
    ) match {
      case Success(v) =>
        CashInOut.updateById(id).withAttributes(
          'statusType -> ActionStatusType.CANCELLED.value)
      case Failure(e) => throw e
    }
        
  /**
   * キャッシュフローをエラー状態にします。
   * <p>処理中に失敗した際に呼び出してください。
   * low: 実際はエラー事由などを引数に取って保持する
   */
  def error()(implicit s: DBSession, dh: DomainHelper): Unit =
    Validator.validateTry(v =>
      v.verify(statusType.isUnprocessed, ErrorKeys.ActionUnprocessing)
    ) match {
      case Success(v) =>
        CashInOut.updateById(id).withAttributes(
          'statusType -> ActionStatusType.ERROR.value)
      case Failure(e) => throw e
    }

}

object CashInOut extends CashInOutMapper {
  
  /** 振込入出金依頼を返します。 */
  def load(id: Long)(implicit s: DBSession): CashInOut =
    findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))

  /** 未処理の振込入出金依頼一覧を検索します。 (可変条件実装例) */
  def find(p: FindCashInOut)(implicit s: DBSession, dh: DomainHelper): List[CashInOut] =
    withAlias(m =>
      findAllBy(
        sqls.toAndConditionOpt(
          p.currency.map(sqls.eq(m.currency, _)),
          Some(sqls.between(m.updateDate, p.updFromDay.atStartOfDay(), DateUtils.dateTo(p.updToDay))),
          if (p.statusTypes.nonEmpty) Some(sqls.in(m.statusType, p.statusTypes.map(_.value))) else None
        ).getOrElse(sqls.empty),
        Seq(m.updateDate.desc)))
    
  /** 当日発生で未処理の振込入出金一覧を検索します。 */
  def findUnprocessed(implicit s: DBSession, dh: DomainHelper): List[CashInOut] =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.eventDay, dh.time.day)
          .and.in(m.statusType, ActionStatusType.unprocessedTypeValues),
        Seq(m.id)))

  /** 未処理の振込入出金一覧を検索します。(口座別) */
  def findUnprocessed(accountId: String, currency: String, withdrawal: Boolean)(implicit s: DBSession): List[CashInOut] =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.accountId, accountId)
          .and.eq(m.currency, currency)
          .and.eq(m.withdrawal, withdrawal)
          .and.in(m.statusType, ActionStatusType.unprocessedTypeValues),
        Seq(m.id)))

  /** 未処理の振込入出金一覧を検索します。(口座別) */
  def findUnprocessed(accountId: String)(implicit s: DBSession): List[CashInOut] =
    withAlias(m =>
      findAllBy(
        sqls
          .eq(m.accountId, accountId)
          .and.in(m.statusType, ActionStatusType.unprocessedTypeValues),
        Seq(m.updateDate.desc)))

  /** 出金依頼をします。 */
  def withdraw(businessDay: BusinessDayHandler, p: RegCashOut)(implicit s: DBSession, dh: DomainHelper): Long = {
    val accountId = p.accountId.getOrElse(dh.actor.id)
    // low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
    val eventDay = businessDay.day
    // low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
    val valueDay = businessDay.day(3)
    // 事前審査
    Validator.validateTry(v => {
      v.verifyField(0 < p.absAmount.signum, "absAmount", DomainErrorKeys.AbsAmountZero)
      v.verifyField(canWithdraw(accountId, p.currency, p.absAmount, valueDay),
         "absAmount", AssetErrorKeys.CashInOutWithdrawAmount)
    }) match {
      case Success(v) =>
        // 出金依頼情報を登録
        val acc = FiAccount.load(accountId, Remarks.CashOut, p.currency)
        val selfAcc = SelfFiAccount.load(Remarks.CashOut, p.currency)
        createWithAttributes(
          'accountId -> accountId,
          'currency -> p.currency,
          'absAmount -> p.absAmount,
          'withdrawal -> true,
          'requestDay -> dh.time.day,
          'requestDate -> dh.time.date,
          'eventDay -> eventDay,
          'valueDay -> valueDay,
          'targetFiCode -> acc.fiCode,
          'targetFiAccountId -> acc.fiAccountId,
          'selfFiCode -> selfAcc.fiCode,
          'selfFiAccountId -> selfAcc.fiAccountId,
          'statusType -> ActionStatusType.UNPROCESSED.value,
          'updateDate -> dh.time.date)
      case Failure(e) => throw e
    }
  }
  private def canWithdraw(accountId: String, currency: String, absAmount: BigDecimal, valueDay: LocalDate)(implicit s: DBSession, dh: DomainHelper): Boolean =
    Asset(accountId).canWithdraw(currency, absAmount, valueDay)
}

trait CashInOutMapper extends SkinnyORMMapper[CashInOut] {
  override def extract(rs: WrappedResultSet, n: ResultName[CashInOut]) =
    CashInOut(
      id = rs.long(n.id),
      accountId = rs.string(n.accountId),
      currency = rs.string(n.currency),
      absAmount = rs.bigDecimal(n.absAmount),
      withdrawal = rs.boolean(n.withdrawal),
      requestDay = rs.localDate(n.requestDay),
      requestDate = rs.localDateTime(n.requestDate),
      eventDay = rs.localDate(n.eventDay),
      valueDay = rs.localDate(n.valueDay),
      targetFiCode = rs.string(n.targetFiCode),
      targetFiAccountId = rs.string(n.targetFiAccountId),
      selfFiCode = rs.string(n.selfFiCode),
      selfFiAccountId = rs.string(n.selfFiAccountId),
      statusType = ActionStatusType.withName(rs.string(n.statusType)),
      cashflowId = rs.longOpt(n.cashflowId),
      updateDate = rs.localDateTime(n.updateDate))
}

/** 振込入出金依頼の検索パラメタ。 low: 通常は顧客視点/社内視点で利用条件が異なる */
case class FindCashInOut(
  currency: Option[String] = None,
  statusTypes: Seq[ActionStatusType] = Seq(),
  updFromDay: LocalDate, updToDay: LocalDate) extends Dto

/** 振込出金の依頼パラメタ。  */
case class RegCashOut(
  accountId: Option[String] = None,
  currency: String,
  absAmount: BigDecimal) extends Dto
