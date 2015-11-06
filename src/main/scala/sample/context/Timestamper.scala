package sample.context

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired

import sample.util.DateUtils
import sample.util.TimePoint

/**
 * 日時ユーティリティコンポーネント。
 */
class Timestamper(clock: Clock) {

  @Autowired(required = false)
  var setting: AppSettingHandler = _

  /** 営業日を返します。 */
  def day(): LocalDate = Option(setting) match {
    case Some(sh) => DateUtils.day(sh.setting(Timestamper.KEY_DAY).str())
    case None => LocalDate.now(clock)
  }

  /** 日時を返します。 */
  def date(): LocalDateTime = LocalDateTime.now(clock)

  /** 営業日/日時を返します。 */
  def tp(): TimePoint = TimePoint(day, date)
  /**
   * 営業日を指定日へ進めます。
   * <p>AppSettingHandlerを設定時のみ有効です。
   * @param day 更新営業日
   */
  def proceedDay(day: LocalDate): Timestamper = {
    Option(setting).map(_.update(Timestamper.KEY_DAY, DateUtils.dayFormat(day)))
    this
  }
}

object Timestamper {
  val KEY_DAY: String = "system.businessDay.day"
  def apply(): Timestamper = apply(Clock.systemDefaultZone())
  def apply(clock: Clock): Timestamper = new Timestamper(clock)
}
