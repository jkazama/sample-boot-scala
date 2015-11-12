package sample.context.audit

import java.time.{LocalDateTime, LocalDate, ZoneId}
import org.apache.commons.lang3.StringUtils
import scalikejdbc.jsr310.Implicits._
import scalikejdbc._
import sample._
import sample.context._
import sample.context.actor.{Actor, ActorRoleType}
import sample.context.orm._
import sample.util._

/**
 * システム利用者の監査ログを表現します。
 */
case class AuditActor(
  /** ID */
  id: Long = -1L, // autogen
  /** 利用者ID */
  actorId: String,
  /** 利用者役割 */
  roleType: ActorRoleType,
  /** 利用者ソース(IP等) */
  source: Option[String] = None,
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

object AuditActor extends AuditActorMapper {

  /** 利用者監査ログを検索します。 */
  def find(p: FindAuditActor)(implicit session: DBSession, dh: DomainHelper): PagingList[AuditActor] =
    PagingList(AuditActor.withAlias(m =>
      findAllByWithLimitOffset(
        sqls.toAndConditionOpt(
          Some(sqls.between(m.startDate, p.fromDay.atStartOfDay(), DateUtils.dateTo(p.toDay))),
          Some(sqls.eq(m.roleType, p.roleType.value)),
          p.statusType.map(stype => sqls.eq(m.statusType, stype.value)),
          p.category.map(sqls.eq(m.category, _)),
          p.actorId.map(aid =>
            sqls.like(m.actorId, p.likeActorId).or(sqls.like(m.source, p.likeActorId))),
          p.keyword.map(key =>
            sqls.like(m.message, p.likeKeyword).or(sqls.like(m.errorReason, p.likeKeyword)))
        ).getOrElse(sqls.empty),
        p.page.size, p.page.firstResult,
        Seq(m.startDate.desc)
      )), p.page)

  /** 利用者監査ログを登録します。 */
  def register(p: RegAuditActor)(implicit session: DBSession, dh: DomainHelper): Long =
    Some(dh.actor).map(actor =>
      AuditActor.createWithAttributes(
        'actorId -> actor.id,
        'roleType -> actor.roleType.value,
        'source -> actor.source,
        'category -> p.category,
        'message -> ConvertUtils.left(p.message, 300),
        'statusType -> ActionStatusType.PROCESSING.value,
        'startDate -> dh.time.date)
    ).get
    
  /** 利用者監査ログを完了状態にします。 */
  def finish(id: Long)(implicit session: DBSession, dh: DomainHelper): Unit = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.PROCESSED.value,
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
  
  /** 利用者監査ログを取消状態にします。 */
  def cancel(id: Long, errorReason: String)(implicit session: DBSession, dh: DomainHelper): Long = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.CANCELLED.value,
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
  
  /** 利用者監査ログを例外状態にします。 */
  def error(id: Long, errorReason: String)(implicit session: DBSession, dh: DomainHelper): Long = {
    val now = dh.time.date
    val m = findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
    updateById(id).withAttributes(
      'statusType -> ActionStatusType.ERROR.value,
      'errorReason -> StringUtils.abbreviate(errorReason, 250),
      'endDate -> now,
      'time -> DateUtils.between(m.startDate, now).get.toMillis())
  }
}

trait AuditActorMapper extends SkinnyORMMapper[AuditActor] {
  override def extract(rs: WrappedResultSet, n: ResultName[AuditActor]) =
    AuditActor(
      id = rs.long(n.id),
      actorId = rs.string(n.actorId),
      roleType = ActorRoleType.withName(rs.string(n.roleType)),
      source = rs.stringOpt(n.source),
      category = rs.string(n.category),
      message = rs.string(n.message),
      statusType = ActionStatusType.withName(rs.string(n.statusType)),
      errorReason = rs.stringOpt(n.errorReason),
      time = rs.longOpt(n.time),
      startDate = rs.localDateTime(n.startDate),
      endDate = rs.localDateTimeOpt(n.endDate))
}

/** 検索パラメタ */
case class FindAuditActor(
  actorId: Option[String] = None,
  category: Option[String] = None,
  keyword: Option[String] = None,
  roleType: ActorRoleType,
  statusType: Option[ActionStatusType] = None,
  fromDay: LocalDate,
  toDay: LocalDate,
  page: Pagination = Pagination()) extends Dto {
  def likeActorId = s"%${actorId.getOrElse("")}%"
  def likeKeyword = s"%${keyword.getOrElse("")}%"
}

/** 登録パラメタ */
case class RegAuditActor(
  category: String,
  message: String) extends Dto
object RegAuditActor {
  def apply(message: String): RegAuditActor = RegAuditActor("default", message)
}
