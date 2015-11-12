package sample.usecase.mail

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import scalikejdbc._
import sample.context.mail.MailHandler
import sample.model.asset._

/**
 * アプリケーション層のサービスメール送信を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 */
@Component
class ServiceMailDeliver {
  
  @Autowired
  private var mail: MailHandler = _
  
  /** トランザクション処理を実行します。 */
  private def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))
  
  /** 出金依頼受付メールを送信します。 */
  //low: サンプルなので未実装。実際は独自にトランザクションを貼って処理を行う
  def sendWithdrawal(cio: CashInOut): Unit = Unit
  
}