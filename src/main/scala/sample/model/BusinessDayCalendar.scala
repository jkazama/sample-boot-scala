package sample.model

import org.springframework.stereotype.Component
import sample.context.Timestamper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import sample.model.master.Holiday
import java.time.LocalDate
import scalikejdbc._
import sample.model.master.RegHoliday
import sample.util.DateUtils
import org.springframework.context.annotation.Lazy

/**
 * ドメインに依存する営業日関連のユーティリティハンドラ。
 */
@Component
class BusinessDayCalendar {
  @Autowired
  private var time: Timestamper = _
  @Autowired(required = false)
  @Lazy
  private var holidayAccessor: HolidayAccessor = _
  
  /** 営業日を返します。 */
  def day: LocalDate = time.day
  
  /** 営業日を返します。 low: そのうちScalaっぽく書き直す */
  def day(daysToAdd: Int): LocalDate = {
    var d = day
    if (0 < daysToAdd) {
      for (i <- 0 until daysToAdd) d = dayNext(d)
    } else {
      for (i <- 0 until (-daysToAdd)) d = dayNext(d)
    }
    d
  }
  private def dayNext(baseDay: LocalDate): LocalDate = {
    var d = baseDay.plusDays(1)
    while (isHolidayOrWeekDay(d)) d = d.plusDays(1)
    d
  }
  private def dayPrevious(baseDay: LocalDate): LocalDate = {
    var d = baseDay.minusDays(1)
    while (isHolidayOrWeekDay(d)) d = d.minusDays(1)
    d
  }
  
  /** 祝日もしくは週末時はtrue。 */
  private def isHolidayOrWeekDay(d: LocalDate): Boolean =
    (DateUtils.isWeekend(d) || isHoliday(d))
  private def isHoliday(d: LocalDate): Boolean =
    Option(holidayAccessor).map(_.get(d).isDefined).getOrElse(false)
}

/** 祝日マスタを検索/登録するアクセサ。 */
@Component
class HolidayAccessor {
  @Cacheable(cacheNames=Array("HolidayAccessor.getHoliday"))
  def get(day: LocalDate)(implicit s: DBSession = Holiday.autoSession): Option[Holiday] = Holiday.get(day)
  @Cacheable(cacheNames=Array("HolidayAccessor.getHoliday"))
  def register(p: RegHoliday)(implicit s: DBSession = Holiday.autoSession): Unit = Holiday.register(p)
}