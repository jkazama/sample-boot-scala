package sample.util

import scala.util.control.Breaks

import com.ibm.icu.text.Transliterator

/** 各種型/文字列変換をサポートします。(ICU4Jライブラリに依存しています) */
trait ConvertUtils {
  private val zenkakuToHan: Transliterator = Transliterator.getInstance("Fullwidth-Halfwidth")
  private val hankakuToZen: Transliterator = Transliterator.getInstance("Halfwidth-Fullwidth")
  private val katakanaToHira: Transliterator = Transliterator.getInstance("Katakana-Hiragana")
  private val hiraganaToKana: Transliterator = Transliterator.getInstance("Hiragana-Katakana")
  
  /** 例外無しにLongへ変換します。(変換できない時はNone) */
  def quietlyLong(value: Any): Option[Long] = try {
    Option(value).map(_.toString().toLong)
  } catch {
    case e: Exception => Option.empty
  }
  
  /** 例外無しにIntへ変換します。(変換できない時はNone) */
  def quietlyInt(value: Any): Option[Int] = try {
    Option(value).map(_.toString().toInt)
  } catch {
    case e: Exception => Option.empty
  }
 
  /** 例外無しにBigDecimalへ変換します。(変換できない時はNone) */
  def quietlyDecimal(value: Any): Option[BigDecimal] = try {
    Option(value).map(v => BigDecimal(v.toString()))
  } catch {
    case e: Exception => Option.empty
  }
  
  /** 例外無しBooleanへ変換します。(変換できない時はfalse) */
  def quietlyBool(value: Any): Boolean = try {
    Option(value).map(_.toString().toBoolean).getOrElse(false)
  } catch {
    case e: Exception => false
  }
  
  /** 全角文字を半角にします。 */
  def zenkakuToHan(text: String): String =
    Option(text).map(zenkakuToHan.transliterate(_)).getOrElse(null)
  
  /** 半角文字を全角にします。 */
  def hankakuToZen(text: String): String =
    Option(text).map(hankakuToZen.transliterate(_)).getOrElse(null)
  
  /** カタカナをひらがなにします。 */
  def katakanaToHira(text: String): String =
    Option(text).map(katakanaToHira.transliterate(_)).getOrElse(null)
  
  /**
   * ひらがな/半角カタカナを全角カタカナにします。
   * <p>low: 実際の挙動は厳密ではないので単体検証(ConvertUtilsSpec)などで事前に確認して下さい。
   */
  def hiraganaToZenKana(text: String): String =
    Option(text).map(hiraganaToKana.transliterate(_)).getOrElse(null)

  /**
   * ひらがな/全角カタカナを半角カタカナにします。
   * <p>low: 実際の挙動は厳密ではないので単体検証(ConvertUtilsSpec)などで事前に確認して下さい。
   */
  def hiraganaToHanKana(text: String): String =
    Option(text).map(v => zenkakuToHan(hiraganaToZenKana(v))).getOrElse(null)

  /** 指定した文字列を抽出します。(サロゲートペア対応) */
  def substring(text: String, start: Int, end: Int): String =
    Option(text).map(v => {
      val spos = v.offsetByCodePoints(0, start)
      val epos = if (text.length < end) text.length else end
      v.substring(spos, v.offsetByCodePoints(spos, epos - start))
    }).getOrElse(null)

  /** 文字列を左から指定の文字数で取得します。(サロゲートペア対応) */
  def left(text: String, len: Int): String = substring(text, 0, len)
  
  /** 文字列を左から指定のバイト数で取得します。 */
  def leftStrict(text: String, lenByte: Int, charset: String): String = {
    val sb = new StringBuilder()
    var cnt: Int = 0
    val scope = new Breaks
    scope.breakable {
      for (i <- 0 until text.length) {
        val v = text.substring(i, i + 1)
        val blen = v.getBytes(charset).length
        if (lenByte < cnt + blen) {
          scope.break()
        } else {
          sb.append(v)
          cnt += blen
        }
      }
    }
    sb.toString()
  }
}
object ConvertUtils extends ConvertUtils
