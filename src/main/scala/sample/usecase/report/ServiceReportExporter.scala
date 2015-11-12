package sample.usecase.report

import java.io.InputStream
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import scalikejdbc._
import sample.context.report.ReportHandler
import sample.model.asset.FindCashInOut

/**
 * アプリケーション層のレポート出力を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 * low: コード量が多くなるため今回のサンプルでは対象外とします。
 */
@Component
class ServiceReportExporter {
  
  @Autowired
  private var report: ReportHandler = _
  
  /** トランザクション処理を実行します。 */
  private def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))

  /**　振込入出金情報をCSV出力します。 */
  //low: バイナリ生成。条件指定を可能にしたオンラインダウンロードを想定。
  def exportCashInOut(): Array[Byte] = Array()
  
  //low: サイズが大きいケースではストリームへ都度書き出しする。
  def exportAnyBigData(ins: InputStream, p: FindCashInOut): Unit = Unit
  
  /**　振込入出金情報を帳票出力します。 */
  //low: 特定のディレクトリへのファイル出力。ジョブ等での利用を想定
  def exportFileCashInOut(baseDay: String): Unit = Unit
}