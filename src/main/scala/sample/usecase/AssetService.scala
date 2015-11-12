package sample.usecase

import org.springframework.stereotype.Service
import sample.context.actor.Actor
import sample.model.asset._
import sample.context.lock.LockType

/**
 * 資産ドメインに対する顧客ユースケース処理。
 */
@Service
class AssetService extends ServiceSupport {

  /** 匿名を除くActorを返します。 */
  override def actor: Actor = ServiceUtils.actorUser(super.actor)
  
  /**
   * 未処理の振込依頼情報を検索します。
   * low: 参照系は口座ロックが必要無いケースであれば@Transactionalでも十分
   * low: CashInOutは情報過多ですがアプリケーション層では公開対象を特定しにくい事もあり、
   * UI層に最終判断を委ねています。
   */
  def findUnprocessedCashOut: List[CashInOut] =
    tx(actor.id, LockType.READ, { implicit session =>
      CashInOut.findUnprocessed(actor.id)
    })

  /**
   * 振込出金依頼をします。
   * low: 公開リスクがあるためUI層には必要以上の情報を返さない事を意識します。
   * low: 監査ログの記録は状態を変えうる更新系ユースケースでのみ行います。
   * low: ロールバック発生時にメールが飛ばないようにトランザクション境界線を明確に分離します。
   * @return 振込出金依頼ID
   */
  def withdraw(p: RegCashOut): Long =
    audit.audit("振込出金依頼をします", () => {
      // 顧客側はログイン利用者で強制上書きして振替
      val cio = tx(actor.id, LockType.WRITE, {implicit session =>
        CashInOut.load(CashInOut.withdraw(businessDay, p.copy(accountId = Some(actor.id))))
      })
      // low: トランザクション確定後に出金依頼を受付した事をメール通知します。
      mail.sendWithdrawal(cio)
      cio.id
    })
  
}
