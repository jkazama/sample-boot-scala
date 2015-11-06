package sample

/**
 * 審査例外を表現します。
 */
class ValidationException(val message: String, val list: Seq[Warn]) extends RuntimeException(message)

object ValidationException {
  def apply(field: String, message: String, messageArgs: Array[AnyRef]): ValidationException =
    new ValidationException(message, List(Warn(field, message, messageArgs)))
  /** フィールドに従属する審査例外を通知するケースで利用してください。 */
  def apply(field: String, message: String): ValidationException = apply(field, message, null)
  /** フィールドに従属しないグローバルな審査例外を通知するケースで利用してください。 */
  def apply(message: String): ValidationException = apply(null, message)
  /** 複数件の審査例外を通知するケースで利用してください。 */
  def apply(list: Seq[Warn]): ValidationException = new ValidationException(list.headOption.map(_.message).getOrElse("none"), list)
}
