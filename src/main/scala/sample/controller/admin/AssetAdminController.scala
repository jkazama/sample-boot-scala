package sample.controller.admin

import java.time.LocalDate

import scala.beans.BeanInfo
import scala.beans.BeanProperty

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation._

import javax.validation.Valid
import sample._
import sample.controller.ControllerSupport
import sample.model.asset._
import sample.model.constraints._
import sample.usecase.AssetAdminService

/**
 * 資産に関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/admin/asset"))
class AssetAdminController extends ControllerSupport {
  
  @Autowired
  private var service: AssetAdminService = _
  
  /** 未処理の振込依頼情報を検索します。 */
  @GetMapping(Array("/cio/"))
  def findCashInOut(@Valid p: FindCashInOutParam): Seq[CashInOut] =
    service.findCashInOut(p.convert)
}

/** FindCashInOutのUI変換パラメタ */
@BeanInfo
class FindCashInOutParam {
  @CurrencyEmpty
  @BeanProperty
  var currency: String = _
  @BeanProperty
  var statusTypes: Array[String] = Array()
  @ISODate
  @BeanProperty
  var updFromDay: LocalDate = _
  @ISODate
  @BeanProperty
  var updToDay: LocalDate = _
  def convert: FindCashInOut =
    FindCashInOut(
        Option(currency), statusTypes.map(ActionStatusType.withName(_)), updFromDay, updToDay)
}
