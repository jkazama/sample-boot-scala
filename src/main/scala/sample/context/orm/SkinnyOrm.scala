package sample.context.orm

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import javax.annotation.{PreDestroy, PostConstruct}
import scalikejdbc.{LoggingSQLAndTimeSettings, GlobalSettings}
import skinny.DBSettings

/**
 * SkinnyのORM初期定義を行います。
 */
@Primary
@Component
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
