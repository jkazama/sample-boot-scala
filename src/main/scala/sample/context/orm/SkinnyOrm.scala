package sample.context.orm

import javax.annotation.{PreDestroy, PostConstruct}
import scalikejdbc.{LoggingSQLAndTimeSettings, GlobalSettings}
import skinny.DBSettings

/**
 * SkinnyのORM初期定義を行います。
 */
class SkinnyOrm {

  @PostConstruct
  def initialize() = {
    GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = true,
      logLevel = 'DEBUG
    )
    DBSettings.initialize()
  }

  @PreDestroy
  def destroy() = DBSettings.destroy()
}
