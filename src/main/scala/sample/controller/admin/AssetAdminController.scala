package sample.controller.admin

import java.time.LocalDate

import scala.beans.BeanInfo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid
import sample._
import sample.model.constraints._
import sample.model.asset._
import sample.usecase.AssetAdminService
import sample.controller.ControllerSupport

/**
 * 資産に関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/admin/asset"))
class AssetAdminController extends ControllerSupport {
  
  @Autowired
  private var service: AssetAdminService = _
  
  /** 未処理の振込依頼情報を検索します。 */
  @RequestMapping(value = Array("/cio/"))
  def findCashInOut(@Valid p: FindCashInOutParam): Seq[CashInOut] =
    service.findCashInOut(p.convert)
}

/** FindCashInOutのUI変換パラメタ */
@BeanInfo
class FindCashInOutParam {
  @CurrencyEmpty
  var currency: String = _
  var statusTypes: Array[String] = Array()
  @ISODate
  var updFromDay: LocalDate = _
  @ISODate
  var updToDay: LocalDate = _
  def convert: FindCashInOut =
    FindCashInOut(
        Option(currency), statusTypes.map(ActionStatusType.withName(_)), updFromDay, updToDay)
}
