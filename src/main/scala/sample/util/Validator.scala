package sample.util

import scala.util.Try

import sample.{Warn, ValidationException}

/**
 * 審査例外の構築概念を表現します。
 * <p>Java版と異なり副作用を発生させないので、複数のチェックを実施する際は
 * メソッドチェインで実行するようにしてください。
 */
case class Validator(warns: Seq[Warn]) {
  
  /** 審査を行います。validがfalseの時に例外を内部にスタックします。 */
  def check(valid: Boolean, message: String): Validator =
    if (valid) this else Validator(warns :+ Warn(message))

  /** 個別属性の審査を行います。validがfalseの時に例外を内部にスタックします。 */
  def checkField(valid: Boolean, field: String, message: String): Validator =
    if (valid) this else Validator(warns :+ Warn(field, message))

  /** 審査を行います。失敗した時は即時に例外を発生させます。 */
  def verify(valid: Boolean, message: String): Validator =
    check(valid, message).verify()

  /** 個別属性の審査を行います。失敗した時は即時に例外を発生させます。 */
  def verifyField(valid: Boolean, field: String, message: String): Validator =
    checkField(valid, field, message).verify()
  
  /** 検証します。事前に行ったcheckで例外が存在していた時は例外を発生させます。 */
  def verify(): Validator =
    if (warns.nonEmpty) throw ValidationException(warns)
    else clear()

  /** 審査例外を保有している時はtrueを返します。  */
  def hasWarn(): Boolean = warns.nonEmpty

  /** 初期化します。 */
  def clear(): Validator = Validator()
  
}
object Validator {
  def apply(): Validator = Validator(Seq())
  
  /** 審査処理を行います。 */
  def validate(proc: Validator => Validator): Unit =
    proc(Validator()).verify()

  def validateTry(proc: Validator => Validator): Try[Unit] = Try(validate(proc))
}

