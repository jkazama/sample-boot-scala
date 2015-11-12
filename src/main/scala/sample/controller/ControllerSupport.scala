package sample.controller

import scala.util.{Try, Success, Failure}
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import sample.context.Timestamper
import sample.context.ResourceBundleHandler
import java.util.Locale
import sample.context.actor.ActorSession
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import scala.collection.JavaConversions._
import org.springframework.web.multipart.MultipartFile
import sample._
import sample.context.report.ReportFile
import javax.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import java.net.URLEncoder
import org.apache.commons.io.IOUtils
import sample.context.orm.SortOrder
import scala.beans.BeanInfo
import sample.context.orm.Sort
import sample.context.orm.Pagination

/** UIコントローラの基底クラス。 */
class ControllerSupport {
  @Autowired
  var msg: MessageSource = _
  @Autowired
  var label: ResourceBundleHandler = _
  @Autowired
  var time: Timestamper = _
  @Autowired
  var session: ActorSession = _
  
  /** i18nメッセージ変換を行います。 */
  protected def msg(message: String): String = msg(message, session.actor.locale)
  protected def msg(message: String, locale: Locale): String = msg.getMessage(message, Array(), locale)
  
  /**
   * リソースファイル([basename].properties)内のキー/値のMap情報を返します。
   * <p>API呼び出し側でi18n対応を行いたい時などに利用してください。
   */
  protected def labels(basename: String): Map[String, String] = labels(basename, session.actor.locale)
  protected def labels(basename: String, locale: Locale): Map[String, String] = label.labels(basename, locale)

  /**
   * 戻り値を生成して返します。(戻り値がプリミティブまたはnullを許容する時はこちらを利用してください)
   * ※nullはJSONバインドされないため、クライアント側でStatusが200にもかかわらず例外扱いされる可能性があります。
   */
  protected def result[T](t: T): ResponseEntity[T] = ResponseEntity.status(HttpStatus.OK).body(t)
  protected def result[T](command: () => T): ResponseEntity[T] = result(command())
  protected def resultMap[T](key: String, t: T): ResponseEntity[java.util.Map[String, T]] = result(Map(key -> t))
  protected def resultMap[T](t: T): ResponseEntity[java.util.Map[String, T]] = resultMap("result", t)
  protected def resultEmpty(command: () => Unit = () => ()): ResponseEntity[Void] = {
    command()
    ResponseEntity.status(HttpStatus.OK).build()
  }
  
  /** ファイルアップロード情報(MultipartFile)をReportFileへ変換します。 */
  protected def uploadFile(file: MultipartFile): ReportFile =
    uploadFile(file, Array())
    
  /**
   * ファイルアップロード情報(MultipartFile)をReportFileへ変換します。
   * <p>acceptExtensionsに許容するファイル拡張子(小文字統一)を設定してください。
   */
  protected def uploadFile(file: MultipartFile, acceptExtensions: Array[String]): ReportFile =
    if (FilenameUtils.isExtension(
        StringUtils.lowerCase(file.getOriginalFilename), acceptExtensions)) {
      Try(ReportFile(file.getOriginalFilename(), file.getBytes())) match {
        case Success(v) => v
        case Failure(e) => throw ValidationException("file", "アップロードファイルの解析に失敗しました")
      }
    } else throw ValidationException("file", "アップロードファイルには[{0}]を指定してください", Array(StringUtils.join(acceptExtensions)))

  /**
   * ファイルダウンロード設定を行います。
   * <p>利用する際は戻り値をUnitで定義するようにしてください。
   */
  protected def exportFile(res: HttpServletResponse, file: ReportFile): Unit =
    exportFile(res, file, MediaType.APPLICATION_OCTET_STREAM_VALUE)
  protected def exportFile(res: HttpServletResponse, file: ReportFile, contentType: String): Unit =
    Try(URLEncoder.encode(file.name,"UTF-8").replace("+", "%20")) match {
      case Success(filename) =>
        res.setContentLength(file.size)
        res.setContentType(contentType)
        res.setHeader("Content-Disposition", "attachment; filename=" + filename)
        IOUtils.write(file.data, res.getOutputStream())
      case Failure(e) => throw ValidationException("ファイル名が不正です")
    }
}

/** ページング系のUI変換パラメタ */
@BeanInfo
class PaginationParam {
  var page: Int = 1
  var size: Int = Pagination.defaultSize
  var total: Long = _
  var ignoreTotal: Boolean = _
  var sort: SortParam = new SortParam()
  def convert: Pagination = Pagination(page, size, Option(total), ignoreTotal, Option(sort).map(_.convert))
}
@BeanInfo
class SortParam {
  var orders: Array[SortOrderParam] = Array()
  def convert: Sort = Sort(if (orders != null) orders.map(_.convert) else Seq())
}
@BeanInfo
class SortOrderParam {
  var property: String = _
  var ascending: Boolean = _
  def convert: SortOrder = SortOrder(property, ascending)
}

