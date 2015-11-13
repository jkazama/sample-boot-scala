package sample.context.audit

import java.time.{ LocalDateTime, LocalDate }

import org.apache.commons.lang3.StringUtils

import sample._
import sample.context._
import sample.context.orm._
import sample.util._
import scalikejdbc.jsr310.Implicits._
import scalikejdbc._

/**
 * システムイベントの監査ログを表現します。
 */
case class AuditEvent(
  /** ID */
  id: Long = -1L, // autogen
  /** カテゴリ */
  category: String,
  /** メッセージ */
  message: String,
  /** 処理ステータス */
  statusType: ActionStatusType,
  /** エラー事由 */
  errorReason: Option[String] = None,
  /** 処理時間(msec) */
  time: Option[Long] = None,
  /** 開始日時 */
  startDate: LocalDateTime,
  /** 終了日時(未完了時はNone) */
  endDate: Option[LocalDateTime] = None) extends Entity

object AuditEvent extends AuditEventMapper {
  
  /** イベント監査ログを検索します。 */
  def find(p: FindAuditEvent)(implicit session: DBSession, dh: DomainHelper): PagingList[AuditEvent] = {
    PagingList(AuditEvent.withAlias(m =>
      findAllByWithLimitOffset(
        sqls.toAndConditionOpt(
          Some(sqls.between(m.startDate, p.fromDay.atStartOfDay(), DateUtils.dateTo(p.toDay))),
          p.statusType.map(stype => sqls.eq(m.statusType, stype.value)),
          p.category.map(sqls.eq(m.category, _)),
          p.keyword.map(k =>
            sqls.like(m.message, p.likeKeyword).or(sqls.like(m.errorReason, p.likeKeyword)))
        ).getOrElse(sqls.empty),
        p.page.size, p.page.firstResult,
        Seq(m.startDate.desc)
      )), p.page)
  }
  
  /** イベント監査ログを登録します。 */
  def register(p: RegAuditEvent)(implicit session: DBSession = autoSession, dh: DomainHelper): Long =
    AuditEvent.createWithAttributes(
      'category -> p.category,
      'message -> ConvertUtils.left(p.message, 300),
      'statusType -> ActionStatusType.PROCESSING.value,
      'startDate -> dh.time.date)
    
  /** 利用者監査ログを完了状態にします。 */
  def finish(id: Long)(implicit session: DBSession = autoSession, dh: DomainHelper): Unit = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.PROCESSED.value,
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
  
  /** 利用者監査ログを取消状態にします。 */
  def cancel(id: Long, errorReason: String)(implicit session: DBSession = autoSession, dh: DomainHelper): Long = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.CANCELLED.value,
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
  
  /** 利用者監査ログを例外状態にします。 */
  def error(id: Long, errorReason: String)(implicit session: DBSession = autoSession, dh: DomainHelper): Long = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.ERROR.value,
      'errorReason -> StringUtils.abbreviate(errorReason, 250),
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
}

trait AuditEventMapper extends SkinnyORMMapper[AuditEvent] {
  override def extract(rs: WrappedResultSet, n: ResultName[AuditEvent]) =
    AuditEvent(
      id = rs.long(n.id),
      category = rs.string(n.category),
      message = rs.string(n.message),
      statusType = ActionStatusType.withName(rs.string(n.statusType)),
      errorReason = rs.stringOpt(n.errorReason),
      time = rs.longOpt(n.time),
      startDate = rs.localDateTime(n.startDate),
      endDate = rs.localDateTimeOpt(n.endDate))
}

/** 検索パラメタ */
case class FindAuditEvent(
  category: Option[String] = None,
  keyword: Option[String] = None,
  statusType: Option[ActionStatusType] = None,
  fromDay: LocalDate,
  toDay: LocalDate,
  page: Pagination = Pagination()) extends Dto {
  def likeKeyword = s"%${keyword.getOrElse("")}%"
}

/** 登録パラメタ */
case class RegAuditEvent(
  category: String,
  message: String) extends Dto
object RegAuditEvent {
  def apply(message: String): RegAuditEvent = RegAuditEvent("default", message)
}
