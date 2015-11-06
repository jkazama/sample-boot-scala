package sample.util

/**
 * 簡易的な入力チェッカーを表現します。
 */
trait Checker {
  /** 文字桁数チェック、max以下の時はtrue。(サロゲートペア対応) */
  def len(v: String, max: Int): Boolean = wordSize(v) <= max
  private def wordSize(v: String): Int = v.codePointCount(0, v.length)
}
object Checker extends Checker
