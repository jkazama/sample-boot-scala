package sample.controller.admin

import java.time.LocalDate

import scala.beans.BeanInfo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._

import javax.validation.Valid
import sample._
import sample.context.security._
import sample.controller.ControllerSupport
import sample.model.constraints._
import sample.model.master._
import sample.usecase.MasterAdminService

/**
 * マスタに関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/admin/master"))
class MasterAdminController extends ControllerSupport {
  
  @Autowired
  private var service: MasterAdminService = _
  @Autowired
  private var securityProps: SecurityProperties = _
  
  /** 社員ログイン状態を確認します。 */
  @RequestMapping(value = Array("/loginStatus"))
  def loginStatus: Boolean = true
  
  /** 社員ログイン情報を取得します。 */
  @RequestMapping(value = Array("/loginStaff"))
  def loadLoginStaff: LoginStaff =
    if (securityProps.auth.enabled) {
      SecurityActorFinder.actorDetails.map(details =>
        LoginStaff(details.actor.id, details.actor.name, details.authorityIds)
      ).getOrElse(throw ValidationException(ErrorKeys.Authentication))
    } else LoginStaff("sample", "sample", Seq()) // for dummy login
  
  /** 休日を登録します。 */
  @RequestMapping(value = Array("/holiday/"), method = Array(RequestMethod.POST))
  def registerHoliday(@Valid p: RegHolidayParam): ResponseEntity[Void] =
    resultEmpty(service.registerHoliday(p.convert))
}

/** クライアント利用用途に絞ったパラメタ */
case class LoginStaff(id: String, name: String, authorities: Seq[String])

/** RegHolidayのUI変換パラメタ */
@BeanInfo
class RegHolidayParam {
  @CategoryEmpty
  var category: String = _
  @Year
  var year: Int = _
  @Valid
  var list: Seq[RegHolidayItemParam] = _
  def convert: RegHoliday = RegHoliday(Option(category), year, list.map(_.convert))
}
@BeanInfo
class RegHolidayItemParam {
  @ISODate
  var day: LocalDate = _
  @Name
  var name: String = _
  def convert: RegHolidayItem = RegHolidayItem(day, name)
}
