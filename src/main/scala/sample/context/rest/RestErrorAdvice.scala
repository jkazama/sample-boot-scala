package sample.context.rest

import java.util.Locale
import java.io.IOException
import javax.validation.ConstraintViolationException
import scala.collection.JavaConverters._
import org.slf4j.{LoggerFactory, Logger}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.{ResponseEntity, HttpStatus}
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.{ExceptionHandler, ControllerAdvice, RestController}
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation._
import org.springframework.context.MessageSourceResolvable
import sample._
import sample.context.actor.ActorSession

/**
 * REST用の例外Map変換サポート。
 * <p>AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 */
@ControllerAdvice(annotations = Array(classOf[RestController]))
class RestErrorAdvice {
  def log: Logger = LoggerFactory.getLogger(getClass())
  @Autowired
  var msg: MessageSource = _
  @Autowired
  var session: ActorSession = _

  /** Servlet例外 */
  @ExceptionHandler(Array(classOf[ServletRequestBindingException]))
  def handleServletRequestBinding(e: ServletRequestBindingException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, "error.ServletRequestBinding"))
  private def locale: Locale = session.actor.locale
  private def handleException(e: Exception, holder: ErrorHolder, status: HttpStatus = HttpStatus.BAD_REQUEST): ResponseEntity[Map[String, Array[String]]] = {
    log.warn(e.getMessage())
    holder.result(HttpStatus.BAD_REQUEST)
  }
  
  /** メディアタイプのミスマッチ例外 */
  @ExceptionHandler(Array(classOf[HttpMediaTypeNotAcceptableException]))
  def handleHttpMediaTypeNotAcceptable(e: HttpMediaTypeNotAcceptableException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, "error.HttpMediaTypeNotAcceptable"))
    
  /** 権限例外 */
  @ExceptionHandler(Array(classOf[AccessDeniedException]))
  def handleAccessDeniedException(e: AccessDeniedException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, ErrorKeys.AccessDenied), HttpStatus.UNAUTHORIZED)

  /** BeanValidation(JSR303)の制約例外 */
  @ExceptionHandler(Array(classOf[ConstraintViolationException]))
  def handleConstraintViolation(e: ConstraintViolationException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, exceptionToWarns(e)))
  private def exceptionToWarns(e: ConstraintViolationException): Seq[Warn] =
    e.getConstraintViolations().asScala.map(v =>
      Warn(v.getPropertyPath.toString(), v.getMessage)).toIndexedSeq

  /** Controllerへのリクエスト紐付け例外 */
  @ExceptionHandler(Array(classOf[BindException]))
  def handleBind(e: BindException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, exceptionToWarns(e)))
  private def exceptionToWarns(e: BindException): Seq[Warn] =
    e.getAllErrors.asScala.map(oe =>
      Warn(field(oe), message(oe), args(oe)))
  private def field(e: ObjectError): String =
    if (e.getCodes.length == 1) parseField(e.getCodes()(0))
    else if (1 < e.getCodes().length) parseField(e.getCodes()(1)) // low: プリフィックスは冗長なので外してます
    else ""
  private def parseField(field: String): String =
    Option(field).map(v => v.substring(v.indexOf('.') + 1)).getOrElse("")
  private def message(e: ObjectError): String =
    if (0 <= e.getCodes()(0).indexOf("typeMismatch")) e.getCodes()(2) else e.getDefaultMessage()
  private def args(e: ObjectError): Array[AnyRef] =
    e.getArguments().filter(v => !v.isInstanceOf[MessageSourceResolvable])

  /** アプリケーション例外 */
  @ExceptionHandler(Array(classOf[ValidationException]))
  def handleValidation(e: ValidationException): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, e))

  /** IO例外（Tomcatの Broken pipe はサーバー側の責務ではないので除外しています) */
  @ExceptionHandler(Array(classOf[IOException]))
  def handleIOException(e: IOException): ResponseEntity[Map[String, Array[String]]] =
    if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
      log.info("クライアント事由で処理が打ち切られました。")
      new ResponseEntity(HttpStatus.OK)
    } else {
      handleException(e)
    }
  
  /** 汎用例外 */
  @ExceptionHandler(Array(classOf[Exception]))
  def handleException(e: Exception): ResponseEntity[Map[String, Array[String]]] =
    handleException(e, ErrorHolder(msg, locale, ErrorKeys.Exception), HttpStatus.INTERNAL_SERVER_ERROR)

}

/**
 * 例外情報のスタックを表現します。
 * <p>スタックした例外情報は{@link #result(HttpStatus)}を呼び出す事でMapを持つResponseEntityへ変換可能です。
 * Mapのkeyはfiled指定値、valueはメッセージキーの変換値(messages-validation.properties)が入ります。
 * <p>{@link #errorGlobal}で登録した場合のキーは空文字となります。
 * <p>クライアント側は戻り値を [{"fieldA": "messageA"}, {"fieldB": "messageB"}]で受け取ります。
 */
case class ErrorHolder(errors: Map[Option[String], Seq[Warn]], msg: MessageSource, locale: Locale) {
  /** グローバルスコープのメッセージを追加して返します */
  def errorGlobal(msgKey: String, msgArgs: Option[Array[AnyRef]]): ErrorHolder =
    copy(errors = errors + (None -> (errors.getOrElse(None, Seq()) :+
        Warn(msg.getMessage(msgKey, msgArgs.getOrElse(Array()), msgKey, locale)))))
  /** フィールドスコープのメッセージを追加して返します */
  def error(field: String, msgKey: String, msgArgs: Option[Array[AnyRef]]): ErrorHolder =
    copy(errors = errors + (Some(field) -> (errors.getOrElse(Some(field), Seq()) :+
        Warn(msgKey, msgArgs.getOrElse(Array())))))
  /** 保有する例外情報をResponseEntityへ変換します。 */
  def result(status: HttpStatus): ResponseEntity[Map[String, Array[String]]] =
      new ResponseEntity[Map[String, Array[String]]](
        errors.map(v => v._1.getOrElse("") -> v._2.map(warn =>
          msg.getMessage(warn.message, warn.messageArgs.getOrElse(Array()), warn.message, locale)).toArray),
        status);
}
object ErrorHolder {
  def apply(msg: MessageSource, locale: Locale): ErrorHolder = ErrorHolder(Map[Option[String], Seq[Warn]](), msg, locale)
  def apply(msg: MessageSource, locale: Locale, e: ValidationException): ErrorHolder = apply(msg, locale, e.list)
  def apply(msg: MessageSource, locale: Locale, warns: Seq[Warn]): ErrorHolder =
    ErrorHolder(warns.groupBy(_.field), msg, locale)
  def apply(msg: MessageSource, locale: Locale, globalMsgKey: String, msgArgs: Option[Array[AnyRef]] = None): ErrorHolder =
    apply(msg, locale).errorGlobal(globalMsgKey, msgArgs = msgArgs) 
}

