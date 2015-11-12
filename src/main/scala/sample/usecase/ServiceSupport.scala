package sample.usecase

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import scalikejdbc._
import sample.context._
import sample.context.actor.Actor
import sample.context.audit.AuditHandler
import sample.context.lock._
import sample.model.BusinessDayHandler
import sample.usecase.report.ServiceReportExporter
import sample.usecase.mail.ServiceMailDeliver
import org.slf4j.LoggerFactory

/**
 * ユースケースサービスの基底クラス。
 */
trait ServiceSupport {
  
  protected val logger = LoggerFactory.getLogger(getClass())
  
  @Autowired
  protected var msg: MessageSource = _

  @Autowired
  protected implicit var dh: DomainHelper = _
  @Autowired
  protected var idLock: IdLockHandler = _

  @Autowired
  protected var audit: AuditHandler = _
  @Autowired
  protected var businessDay: BusinessDayHandler = _
  
  @Autowired
  protected var mail: ServiceMailDeliver = _
  @Autowired
  private var report: ServiceReportExporter = _

  /** トランザクション処理を実行します。 */
  protected def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))

  /** 口座ロック付でトランザクション処理を実行します。 */
  protected def tx[T](accountId: String, lockType: LockType, callable: DBSession => T): T =
    idLock.call(accountId, lockType, () => tx(implicit s => callable(s)))

  /** i18nメッセージ変換を行います。 */
  protected def msg(message: String): String =
    msg.getMessage(message, null, message, actor.locale)

  /** ログイン中の利用者を返します。 */
  protected def actor: Actor = dh.actor
  
}