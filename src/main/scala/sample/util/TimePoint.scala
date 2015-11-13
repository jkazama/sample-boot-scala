package sample.util

import java.time.{Clock, LocalDateTime, LocalDate}

import sample.model.constraints.{ISODate, ISODateTime}

/**
 * 日付と日時のペアを表現します。
 * <p>0:00に営業日切り替えが行われないケースなどでの利用を想定しています。
 */
case class TimePoint(
  /** 日付(営業日) */
  @ISODate
  day: LocalDate,
  /** 日付におけるシステム日時 */
  @ISODateTime
  date: LocalDateTime) {

  /** 指定日付と同じか。(day == targetDay) */
  def equalsDay(targetDay: LocalDate): Boolean = day.compareTo(targetDay) == 0

  /** 指定日付よりも前か。(day &lt; targetDay) */
  def beforeDay(targetDay: LocalDate): Boolean = day.compareTo(targetDay) < 0

  /** 指定日付以前か。(day &lt;= targetDay) */
  def beforeEqualsDay(targetDay: LocalDate): Boolean = day.compareTo(targetDay) <= 0

  /** 指定日付よりも後か。(targetDay &lt; day) */
  def afterDay(targetDay: LocalDate): Boolean = 0 < day.compareTo(targetDay)

  /** 指定日付以降か。(targetDay &lt;= day) */
  def afterEqualsDay(targetDay: LocalDate): Boolean = 0 <= day.compareTo(targetDay)
}

object TimePoint {
  def apply(day: LocalDate): TimePoint = TimePoint(day, day.atStartOfDay())
  def apply(): TimePoint = apply(Clock.systemDefaultZone())
  def apply(clock: Clock): TimePoint = Option(LocalDateTime.now(clock)).map(now => TimePoint(now.toLocalDate(), now)).get
}
