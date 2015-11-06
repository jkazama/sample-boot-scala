package sample

/**
 * 処理時の実行例外を表現します。
 * <p>復旧不可能なシステム例外をラップする目的で利用してください。
 */
class InvocationException(val message: String, val cause: Throwable) extends RuntimeException(message, cause)

object InvocationException {
  def apply(message: String, cause: Throwable): InvocationException = new InvocationException(message, cause)
  def apply(message: String): InvocationException = apply(message, null)
  def apply(cause: Throwable): InvocationException = apply(null, cause)
}
