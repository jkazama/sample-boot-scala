package sample.context

import java.time.{Clock, LocalDate, LocalDateTime}

import org.springframework.beans.factory.annotation.Autowired

import sample.util._

/**
 * 日時ユーティリティコンポーネント。
 */
class Timestamper(clock: Clock) {

  @Autowired(required = false)
  var setting: AppSettingHandler = _

  /** 営業日を返します。 */
  def day: LocalDate = Option(setting) match {
    case Some(sh) =>
      DateUtils.day(sh.setting(Timestamper.KeyDay).str())
    case None => LocalDate.now(clock)
  }

  /** 日時を返します。 */
  def date: LocalDateTime = LocalDateTime.now(clock)

  /** 営業日/日時を返します。 */
  def tp: TimePoint = TimePoint(day, date)
  /**
   * 営業日を指定日へ進めます。
   * <p>AppSettingHandlerを設定時のみ有効です。
   * @param day 更新営業日
   */
  def proceedDay(day: LocalDate): Timestamper = {
    Option(setting).map(_.update(Timestamper.KeyDay, DateUtils.dayFormat(day)))
    this
  }
}

object Timestamper {
  val KeyDay: String = "system.businessDay.day"
  def apply(): Timestamper = apply(Clock.systemDefaultZone())
  def apply(clock: Clock): Timestamper = new Timestamper(clock)
}
