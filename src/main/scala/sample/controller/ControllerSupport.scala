package sample.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import sample.context.Timestamper
import sample.context.ResourceBundleHandler
import java.util.Locale
import sample.context.actor.ActorSession
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import scala.collection.JavaConversions._

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
  def msg(message: String): String = msg(message, session.actor().locale)
  def msg(message: String, locale: Locale): String = msg.getMessage(message, Array(), locale)
	
	/**
	 * リソースファイル([basename].properties)内のキー/値のMap情報を返します。
	 * <p>API呼び出し側でi18n対応を行いたい時などに利用してください。
	 */
  def labels(basename: String): Map[String, String] = labels(basename, session.actor().locale)
  def labels(basename: String, locale: Locale): Map[String, String] = label.labels(basename, locale)

	/**
	 * 戻り値を生成して返します。(戻り値がプリミティブまたはnullを許容する時はこちらを利用してください)
	 * ※nullはJSONバインドされないため、クライアント側でStatusが200にもかかわらず例外扱いされる可能性があります。
	 */
  def result[T](t: T): ResponseEntity[T] = ResponseEntity.status(HttpStatus.OK).body(t)
  def result[T](command: Unit => T): ResponseEntity[T] = result(command())
  def resultMap[T](key: String, t: T): ResponseEntity[java.util.Map[String, T]] = result(Map(key -> t))
  def resultMap[T](t: T): ResponseEntity[java.util.Map[String, T]] = resultMap("result", t)
  def resultEmpty(command: Unit => Unit = _ => ()): ResponseEntity[Void] = {
    command()
    ResponseEntity.status(HttpStatus.OK).build()
  }
//	/** ファイルアップロード情報(MultipartFile)をReportFileへ変換します。 */
//	protected ReportFile uploadFile(final MultipartFile file) {
//		return uploadFile(file, (String[])null);
//	}
//
//	/**
//	 * ファイルアップロード情報(MultipartFile)をReportFileへ変換します。
//	 * <p>acceptExtensionsに許容するファイル拡張子(小文字統一)を設定してください。
//	 */
//	protected ReportFile uploadFile(final MultipartFile file, final String... acceptExtensions) {
//		String fname = StringUtils.lowerCase(file.getOriginalFilename());
//		if (acceptExtensions != null && !FilenameUtils.isExtension(fname, acceptExtensions)) {
//			throw new ValidationException("file", "アップロードファイルには[{0}]を指定してください",
//					new String[]{ StringUtils.join(acceptExtensions) });
//		}
//		try {
//			return new ReportFile(file.getOriginalFilename(), file.getBytes());
//		} catch (IOException e) {
//			throw new ValidationException("file", "アップロードファイルの解析に失敗しました");
//		}
//	}
//
//	/**
//	 * ファイルダウンロード設定を行います。
//	 * <p>利用する際は戻り値をvoidで定義するようにしてください。
//	 */
//	protected void exportFile(final HttpServletResponse res, final ReportFile file) {
//		exportFile(res, file, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//	}
//
//	protected void exportFile(final HttpServletResponse res, final ReportFile file, final String contentType) {
//		String filename;
//		try {
//			filename = URLEncoder.encode(file.getName(),"UTF-8").replace("+", "%20");
//		} catch (Exception e) {
//			throw new ValidationException("ファイル名が不正です");
//		}
//		res.setContentLength(file.size());
//		res.setContentType(contentType);
//		res.setHeader("Content-Disposition",
//				"attachment; filename=" + filename);
//		try {
//			IOUtils.write(file.getData(), res.getOutputStream());
//		} catch (IOException e) {
//			throw new ValidationException("ファイル出力に失敗しました");
//		}
//	}

}
