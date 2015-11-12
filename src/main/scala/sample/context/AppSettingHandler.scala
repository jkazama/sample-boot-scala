package sample.context

import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.stereotype.Component

/**
 * アプリケーション設定情報に対するアクセス手段を提供します。
 */
@Component
class AppSettingHandler {

  /** アプリケーション設定情報を取得します。 */
  @Cacheable(cacheNames = Array("AppSettingHandler.appSetting"), key = "#id")
  def setting(id: String): AppSetting = AppSetting.load(id)

  /** アプリケーション設定情報を変更します。 */
  @CacheEvict(cacheNames = Array("AppSettingHandler.appSetting"), key = "#id")
  def update(id: String, value: String): AppSettingHandler = {
    AppSetting.update(id, value)
    this
  }

}
