package sample.context

import sample.UnitSpecSupport
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AppSettingSpec extends UnitSpecSupport {

  behavior of "口座設定"

  it should "口座設定が取得できる" in { implicit session =>
    AppSetting.createWithAttributes('id -> "a", 'value -> "値A")
    AppSetting.createWithAttributes('id -> "b", 'outline -> "概要" ,'value -> "値B")
    AppSetting.find(FindAppSetting(None)).size should be(2)
    AppSetting.find(FindAppSetting(Some("要"))).size should be(1)
    AppSetting.find(FindAppSetting(Some("a"))).size should be(1)
  }
}
