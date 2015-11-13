package sample.controller.system

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._

import sample.controller.ControllerSupport
import sample.usecase._

/**
 * システムジョブのUI要求を処理します。
 * low: 通常はバッチプロセス(または社内プロセスに内包)を別途作成して、ジョブスケジューラから実行される方式になります。
 * ジョブの負荷がオンライン側へ影響を与えないよう事前段階の設計が重要になります。
 * low: 社内/バッチプロセス切り出す場合はVM分散時の情報/排他同期を意識する必要があります。(DB同期/メッセージング同期/分散製品の利用 等)
 */
@RestController
@RequestMapping(Array("/api/system/job"))
class JobController extends ControllerSupport {

  @Autowired
  private var asset: AssetAdminService = _
  @Autowired
  private var system: SystemAdminService = _

  /** 営業日を進めます。 */
  @RequestMapping(value = Array("/daily/processDay"), method = Array(RequestMethod.POST))
  def processDay(): ResponseEntity[Void] =
    resultEmpty(system.processDay())
  
  /** 振込出金依頼を締めます。 */
  @RequestMapping(value = Array("/daily/closingCashOut"),  method = Array(RequestMethod.POST))
  def closingCashOut(): ResponseEntity[Void] =
    resultEmpty(asset.closingCashOut())

  /** キャッシュフローを実現します。 */
  @RequestMapping(value = Array("/daily/realizeCashflow"),  method = Array(RequestMethod.POST))
  def realizeCashflow(): ResponseEntity[Void] =
    resultEmpty(asset.realizeCashflow())

}
