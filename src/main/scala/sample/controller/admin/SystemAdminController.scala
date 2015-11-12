package sample.controller.admin

import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull
import scala.beans.BeanInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import sample._
import sample.context.actor._
import sample.context.audit._
import sample.context.orm.PagingList
import sample.model.constraints._
import sample.usecase.SystemAdminService
import sample.controller._
import sample.context.AppSetting
import sample.context.FindAppSetting
import org.springframework.http.ResponseEntity

/**
 * システムに関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/admin/system"))
class SystemAdminController extends ControllerSupport {
  
  @Autowired
  private var service: SystemAdminService = _
  
  /** 利用者監査ログを検索します。 */
  @RequestMapping(value = Array("/audit/actor/"))
  def findAuditActor(@Valid p: FindAuditActorParam): PagingList[AuditActor] =
    service.findAuditActor(p.convert)

  /** イベント監査ログを検索します。 */
  @RequestMapping(value = Array("/audit/event/"))
  def findAuditEvent(@Valid p: FindAuditEventParam): PagingList[AuditEvent] =
    service.findAuditEvent(p.convert)
    
  /** アプリケーション設定一覧を検索します。 */
  @RequestMapping(value = Array("/setting/"))
  def findAppSetting(@Valid p: FindAppSettingParam): Seq[AppSetting] =
    service.findAppSetting(p.convert)

  /** アプリケーション設定情報を変更します。 */
  @RequestMapping(value = Array("/setting/{id}"), method = Array(RequestMethod.POST))
  def changeAppSetting(@PathVariable id: String, value: String): ResponseEntity[Void] =
    resultEmpty(() => service.changeAppSetting(id, value))
}

/** FindAuditActorのUI変換パラメタ */
@BeanInfo
class FindAuditActorParam {
  @IdStrEmpty
  var actorId: String = _
  @CategoryEmpty
  var category: String = _
  @DescriptionEmpty
  var keyword: String = _
  @NotNull
  var roleType: String = "USER"
  var statusType: String = _
  @ISODate
  var fromDay: LocalDate = _
  @ISODate
  var toDay: LocalDate = _
  @NotNull
  var page: PaginationParam = new PaginationParam()
  def convert: FindAuditActor =
    FindAuditActor(Option(actorId), Option(category), Option(keyword),
        ActorRoleType.withName(roleType),
        Option(statusType).map(ActionStatusType.withName(_)),
        fromDay, toDay, page.convert)
}

/** FindAuditEventのUI変換パラメタ */
@BeanInfo
class FindAuditEventParam {
  @CategoryEmpty
  var category: String = _
  @DescriptionEmpty
  var keyword: String = _
  var statusType: String = _
  @ISODate
  var fromDay: LocalDate = _
  @ISODate
  var toDay: LocalDate = _
  @NotNull
  var page: PaginationParam = new PaginationParam()
  def convert: FindAuditEvent =
    FindAuditEvent(Option(category), Option(keyword),
        Option(statusType).map(ActionStatusType.withName(_)),
        fromDay, toDay, page.convert)
}

/** FindAppSettingのUI変換パラメタ */
@BeanInfo
class FindAppSettingParam {
  @DescriptionEmpty
  var keyword: String = _
  def convert: FindAppSetting = FindAppSetting(Option(keyword))
}

