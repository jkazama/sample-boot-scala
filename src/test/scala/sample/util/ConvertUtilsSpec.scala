package sample.util

import org.junit.runner.RunWith

import org.scalatest.ShouldMatchers
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import ConvertUtils._

@RunWith(classOf[JUnitRunner])
class ConvertUtilsSpec extends WordSpec with ShouldMatchers {
  
  "変換ユーティリティ検証" should {
    "例外無視変換" in {
      quietlyLong("8") should be (Some(8L))
      quietlyLong("a") should be (None)
      quietlyInt("8") should be (Some(8))
      quietlyInt("a") should be (None)
      quietlyDecimal("8.3") should be (Some(BigDecimal("8.3")))
      quietlyDecimal("a") should be (None)
      quietlyBool("true") should be (true)
      quietlyBool("a") should be (false)
    }
    
    "文字列変換" in {
      zenkakuToHan("aA19ａＡ１９あアｱ") should be ("aA19aA19あｱｱ")
      hankakuToZen("aA19ａＡ１９あアｱ") should be ("ａＡ１９ａＡ１９あアア")
      katakanaToHira("aA19ａＡ１９あアｱ") should be ("aA19ａＡ１９あああ")
      hiraganaToZenKana("aA19ａＡ１９あアｱ") should be ("aA19ａＡ１９アアア")
      hiraganaToHanKana("aA19ａＡ１９あアｱ") should be ("aA19aA19ｱｱｱ")
    }
    
    "桁数操作及びサロゲートペア対応" in {
      substring("あ𠮷い", 0, 3) should be ("あ𠮷い")
      substring("あ𠮷い", 1, 2) should be ("𠮷")
      substring("あ𠮷い", 1, 3) should be ("𠮷い")
      substring("あ𠮷い", 2, 3) should be ("い")
      left("あ𠮷い", 2) should be ("あ𠮷")
      leftStrict("あ𠮷い", 6, "UTF-8") should be ("あ𠮷")
    }
  }

}