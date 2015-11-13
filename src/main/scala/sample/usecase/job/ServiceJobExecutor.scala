package sample.usecase.job

import org.springframework.stereotype.Component

import scalikejdbc._

/**
 * アプリケーション層のジョブ実行を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で 呼び出さないように注意してください。
 */
@Component
class ServiceJobExecutor {
   
  /** トランザクション処理を実行します。 */
  private def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))
    
}