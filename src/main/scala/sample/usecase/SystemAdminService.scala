package sample.usecase

import org.springframework.stereotype.Service
import sample.context.orm.PagingList
import sample.context.audit._
import sample.context.FindAppSetting
import sample.context.AppSetting

/**
 * システムドメインに対する社内ユースケース処理。
 */
@Service
class SystemAdminService extends ServiceSupport {

  /** 利用者監査ログを検索します。 */
  def findAuditActor(p: FindAuditActor): PagingList[AuditActor] =
    tx(implicit session => AuditActor.find(p))
  
  /** イベント監査ログを検索します。 */
  def findAuditEvent(p: FindAuditEvent): PagingList[AuditEvent] =
    tx(implicit session => AuditEvent.find(p))

  /** アプリケーション設定一覧を検索します。 */
  def findAppSetting(p: FindAppSetting): Seq[AppSetting] =
    tx(implicit session => AppSetting.find(p))

  def changeAppSetting(id: String, value: String): Unit =
    audit.audit("アプリケーション設定情報を変更する", () =>
      dh.settingSet(id, value))
      
  def processDay(): Unit =
    audit.audit("営業日を進める", () =>
      dh.time.proceedDay(businessDay.day(1)))

}