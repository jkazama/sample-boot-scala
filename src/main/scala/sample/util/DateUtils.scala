package sample.util

import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.{ChronoField, TemporalAccessor, TemporalQuery}

/**
 * 頻繁に利用される日時ユーティリティを表現します。
 */
trait DateUtils {
  private val WeekendQuery: WeekendQuery = new WeekendQuery()

  /** 指定された文字列(YYYY-MM-DD)を元に日付へ変換します。 */
  def day(dayStr: String): LocalDate = dayOpt(dayStr).getOrElse(null)
  def dayOpt(dayStr: String): Option[LocalDate] =
    Option(dayStr).map(d => LocalDate.parse(d.trim(), DateTimeFormatter.ISO_LOCAL_DATE))

  /** 指定された文字列とフォーマット型を元に日時へ変換します。 */
  def date(dateStr: String, formatter: DateTimeFormatter): LocalDateTime = dateOpt(dateStr, formatter).getOrElse(null)
  def dateOpt(dateStr: String, formatter: DateTimeFormatter): Option[LocalDateTime] =
    Option(dateStr).map(dt => LocalDateTime.parse(dt.trim, formatter))

  /** 指定された文字列とフォーマット文字列を元に日時へ変換します。 */
  def date(dateStr: String, format: String): LocalDateTime = date(dateStr, DateTimeFormatter.ofPattern(format))
  def dateOpt(dateStr: String, format: String): Option[LocalDateTime] = dateOpt(dateStr, DateTimeFormatter.ofPattern(format))

  /** 指定された日付を日時へ変換します。*/
  def dateByDay(day: LocalDate): LocalDateTime = dateByDayOpt(day).getOrElse(null)
  def dateByDayOpt(day: LocalDate): Option[LocalDateTime] = Option(day).map((v) => v.atStartOfDay())

  /** 指定した日付の翌日から1msec引いた日時を返します。 */
  def dateTo(day: LocalDate): LocalDateTime = dateToOpt(day).getOrElse(null)
  def dateToOpt(day: LocalDate): Option[LocalDateTime] = Option(day).map((v) => v.atTime(23, 59, 59))

  /** 指定された日時型とフォーマット型を元に文字列(YYYY-MM-DD)へ変更します。 */
  def dayFormat(day: LocalDate): String = dayFormatOpt(day).getOrElse(null)
  def dayFormatOpt(day: LocalDate): Option[String] = Option(day).map((v) => v.format(DateTimeFormatter.ISO_LOCAL_DATE))

  /** 指定された日時型とフォーマット型を元に文字列へ変更します。 */
  def dateFormat(date: LocalDateTime, formatter: DateTimeFormatter): String = dateFormatOpt(date, formatter).getOrElse(null)
  def dateFormatOpt(date: LocalDateTime, formatter: DateTimeFormatter): Option[String] = Option(date).map((v) => v.format(formatter))

  /** 指定された日時型とフォーマット文字列を元に文字列へ変更します。 */
  def dateFormat(date: LocalDateTime, format: String): String = dateFormatOpt(date, format).getOrElse(null)
  def dateFormatOpt(date: LocalDateTime, format: String): Option[String] = Option(date).map((v) => v.format(DateTimeFormatter.ofPattern(format)))

  /** 日付の間隔を取得します。 */
  def between(start: LocalDate, end: LocalDate): Option[Period] =
    if (start == null || end == null) Option.empty
    else Option(Period.between(start, end))

  /** 日時の間隔を取得します。 */
  def between(start: LocalDateTime, end: LocalDateTime): Option[Duration] =
    if (start == null || end == null) Option.empty
    else Option(Duration.between(start, end))

  /** 指定営業日が週末(土日)か判定します。(引数は必須) */
  def isWeekend(day: LocalDate): Boolean = day.query(WeekendQuery)

  /** 指定年の最終日を取得します。 */
  def dayTo(year: Int): LocalDate = LocalDate.ofYearDay(year, if (Year.of(year).isLeap()) 366 else 365)

}
object DateUtils extends DateUtils

/** 週末判定用のTemporalQuery&gt;Boolean&lt;を表現します。 */
class WeekendQuery extends TemporalQuery[Boolean] {
  override def queryFrom(temporal: TemporalAccessor): Boolean =
    Array(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(
      DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK)))
}
