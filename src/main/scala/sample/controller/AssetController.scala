package sample.controller

import java.time._

import scala.beans.BeanProperty

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._

import javax.validation.Valid
import sample.ActionStatusType
import sample.model.asset._
import sample.model.constraints._
import sample.usecase.AssetService
import java.beans.SimpleBeanInfo

/**
 * 資産に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/asset"))
class AssetController extends ControllerSupport {
  
  @Autowired
  private var service: AssetService = _
  
  /** 未処理の振込依頼情報を検索します。 */
  @GetMapping(value = Array("/cio/unprocessedOut/"))
  def findUnprocessedCashOut: Seq[CashOutUI] =
    service.findUnprocessedCashOut.map(CashOutUI(_))

  /**
   * 振込出金依頼をします。
   * low: RestControllerの標準の振る舞いとしてvoidやプリミティブ型はJSON化されないので注意してください。
   * (解析時の優先順位の関係だと思いますが)
   */
  @PostMapping(Array("/cio/withdraw"))
  def withdraw(@Valid p: RegCashOutParam): ResponseEntity[Long] =
    result(service.withdraw(p.convert))

}

/** 振込出金依頼情報の表示用Dto */
case class CashOutUI(id: Long, currency: String, absAmount: BigDecimal,
  requestDay: LocalDate, requestDate: LocalDateTime, eventDay: LocalDate, valueDay: LocalDate,
  statusType: ActionStatusType, updateDate: LocalDateTime, cashflowId: Option[Long])
object CashOutUI {
  def apply(cio: CashInOut): CashOutUI =
    CashOutUI(cio.id, cio.currency, cio.absAmount, cio.requestDay, cio.requestDate, cio.eventDay, cio.valueDay,
      cio.statusType, cio.updateDate, cio.cashflowId)
}

/**
 * RegCashOutのUI連携DTO。
 * low: SpringMVCで引数に利用するDtoはJavaBeansの仕様を満たす必要があるため、
 * 別途詰め替えクラスを用意する必要があります。(取りまとめてtraitなどで分離すると保守性が上がります)
 * ※戻り値で利用するEntity/DTOには必要ありません。
 * ※本来BeanPropertyの付与は必要ありませんが、Spring Boot 1.3.RELEASE時点で適切なバインドが
 * されない事が確認されたため明示的に付与しています。
 */
@SimpleBeanInfo
class RegCashOutParam {
  @Currency
  @BeanProperty
  var currency: String = _
  @AbsAmount
  @BeanProperty
  var absAmount: java.math.BigDecimal = _
  def convert: RegCashOut = RegCashOut(None, currency, absAmount)
}
