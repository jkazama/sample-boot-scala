package sample.context.audit

import scala.util.{ Try, Success, Failure }

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import sample._
import sample.context._
import sample.context.actor.ActorSession
import scalikejdbc._

/**
 * 利用者監査やシステム監査(定時バッチや日次バッチ等)などを取り扱います。
 * <p>暗黙的な適用を望む場合は、AOPとの連携も検討してください。 
 * <p>対象となるログはLoggerだけでなく、システムスキーマの監査テーブルへ書きだされます。
 * (開始時と完了時で別TXにする事で応答無し状態を検知可能)
 */
@Component
class AuditHandler {
  val LoggerActor: Logger = LoggerFactory.getLogger("Audit.Actor")
  val LoggerEvent: Logger = LoggerFactory.getLogger("Audit.Event")
  val LoggerSystem: Logger = LoggerFactory.getLogger(classOf[AuditHandler])

  @Autowired
  private var session: ActorSession = _
  @Autowired
  private var persister: AuditPersister = _

  /** 与えた処理に対し、監査ログを記録します。 */
  def audit[T](message: String, callable: => T): T =
    audit("default", message, callable)
  def audit[T](category: String, message: String, callable: => T): T = {
    logger.trace(msg(message, "[開始]"))
    val start = System.currentTimeMillis()
    try {
      val v =
        if (session.actor.roleType.system) callEvent(category, message, callable)
        else callAudit(category, message, callable);
      logger.info(msg(message, "[完了]", Some(start)))
      v
    } catch {
      case e: ValidationException =>
        logger.warn(msg(message, "[審例]", Some(start)))
        throw e
      case e: RuntimeException =>
        logger.error(msg(message, "[例外]", Some(start)))
        throw e
      case e: Exception =>
        logger.error(msg(message, "[例外]", Some(start)))
        throw InvocationException(ErrorKeys.Exception, e)
    }
  }
  
  private def logger: Logger = if (session.actor.roleType.system) LoggerEvent else LoggerActor
  
  private def msg(message: String, prefix: String, startMillis: Option[Long] = None): String = {
    val actor = session.actor
    val sb = new StringBuilder(s"${prefix} ")
    if (actor.roleType.notSystem) {
      sb.append(s"[${actor.id}] ")
    }
    sb.append(message);
    if (startMillis.isDefined) {
      sb.append(s" [${System.currentTimeMillis() - startMillis.get} ms]")
    }
    return sb.toString()
  }

  def callAudit[T](category: String, message: String, callable: => T): T = {
    var id: Option[Long] = Option.empty
    try {
      Try(id = Option(persister.startActor(RegAuditActor(category, message)))) match {
        case Success(v) => // nothing
        case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
      }
      callable
    } catch {
      case e: ValidationException =>
        Try(id.map(persister.cancelActor(_, e.getMessage))) match {
          case Success(v) => // nothing
          case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
        }
        throw e
      case e: Exception =>
        Try(id.map(persister.errorActor(_, e.getMessage))) match {
          case Success(v) => // nothing
          case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
        }
        throw e
    }    
  }
  
  def callEvent[T](category: String, message: String, callable: => T): T = {
    var id: Option[Long] = Option.empty
    try {
      Try(id = Option(persister.startEvent(RegAuditEvent(category, message)))) match {
        case Success(v) => // nothing
        case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
      }
      callable
    } catch {
      case e: ValidationException =>
        Try(id.map(persister.cancelEvent(_, e.getMessage))) match {
          case Success(v) => // nothing
          case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
        }
        throw e
      case e: Exception =>
        Try(id.map(persister.errorEvent(_, e.getMessage))) match {
          case Success(v) => // nothing
          case Failure(ex) => LoggerSystem.error(ex.getMessage, ex)
        }
        throw e
    }    
  } 
}
/**
 * 監査ログをシステムスキーマへ永続化します。
 */
@Component
class AuditPersister {
  @Autowired
  private implicit var dh: DomainHelper = _
  def startActor(p: RegAuditActor)(implicit s: DBSession = AuditActor.autoSession): Long =
    AuditActor.register(p)
  def finishActor(id: Long)(implicit s: DBSession = AuditActor.autoSession): Unit =
    AuditActor.finish(id)
  def cancelActor(id: Long, errorReason: String)(implicit s: DBSession = AuditActor.autoSession): Unit =
    AuditActor.cancel(id, errorReason)
  def errorActor(id: Long, errorReason: String)(implicit s: DBSession = AuditActor.autoSession): Unit =
    AuditActor.error(id, errorReason)
  def startEvent(p: RegAuditEvent)(implicit s: DBSession = AuditEvent.autoSession): Long =
    AuditEvent.register(p)
  def finishEvent(id: Long)(implicit s: DBSession = AuditEvent.autoSession): Unit =
    AuditEvent.finish(id)
  def cancelEvent(id: Long, errorReason: String)(implicit s: DBSession = AuditEvent.autoSession): Unit =
    AuditEvent.cancel(id, errorReason)
  def errorEvent(id: Long, errorReason: String)(implicit s: DBSession = AuditEvent.autoSession): Unit =
    AuditEvent.error(id, errorReason)
}
