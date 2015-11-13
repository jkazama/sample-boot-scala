package sample

/**
 * フィールドスコープの審査例外トークン。
 */
case class Warn(field: Option[String], message: String, messageArgs: Option[Array[AnyRef]]) {
  /** フィールドに従属しないグローバル例外時はtrue */
  def global(): Boolean = field.isEmpty
}
object Warn {
  def apply(field: String, message: String, messageArgs: Array[AnyRef]): Warn = Warn(Option(field), message, Option(messageArgs))
  def apply(field: String, message: String): Warn = apply(field, message, null)
  def apply(message: String, messageArgs: Array[AnyRef]): Warn = apply(null, message, messageArgs)
  def apply(message: String): Warn = apply(null, message)
}
