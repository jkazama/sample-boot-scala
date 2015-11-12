package sample.usecase

import scala.util.{Try, Success, Failure}
import org.springframework.stereotype.Service
import sample.context.lock.LockType
import sample.model.asset._

/**
 * 資産ドメインに対する社内ユースケース処理。
 */
@Service
class AssetAdminService extends ServiceSupport {
  
  /**
   * 振込入出金依頼を検索します。
   * low: 口座横断的なので割り切りでREADロックはかけません。
   */
  def findCashInOut(p: FindCashInOut): Seq[CashInOut] =
    tx(implicit session => CashInOut.find(p))
 
  /**
   * 振込出金依頼を締めます。
   */
  def closingCashOut(): Unit =
    audit.audit("振込出金依頼の締め処理をする", () =>
      tx(implicit session =>
        //low: 以降の処理は口座単位でfilter束ねしてから実行する方が望ましい。
        CashInOut.findUnprocessed.foreach(cio =>
          idLock.call(cio.accountId, LockType.WRITE, () =>
            Try(cio.process()) match {
              case Success(v) => // nothing.
              case Failure(e) =>
                logger.error(s"[${cio.id}] 振込出金依頼の締め処理に失敗しました。", e)
                Try(cio.error()) match {
                  case Success(v) => // nothing.
                  case Failure(ex) => //low: 2重障害(恐らくDB起因)なのでloggerのみの記載に留める
                }
            }
          )
        )
      )
    )
    
  /**
   * キャッシュフローを実現します。
   * <p>受渡日を迎えたキャッシュフローを残高に反映します。
   */
  def realizeCashflow(): Unit =
    audit.audit("キャッシュフローを実現する", () =>
      tx(implicit session =>
        //low: 日回し後の実行を想定
        Cashflow.findDoRealize(dh.time.day).foreach(cf =>
          idLock.call(cf.accountId, LockType.WRITE, () =>
            Try(cf.realize()) match {
              case Success(v) => // nothing.
              case Failure(e) =>
                logger.error(s"[${cf.id}] キャッシュフローの実現に失敗しました。", e)
                Try(cf.error()) match {
                  case Success(v) => // nothing.
                  case Failure(ex) => //low: 2重障害(恐らくDB起因)なのでloggerのみの記載に留める
                }
            }
          )
        )
      )
    )

}