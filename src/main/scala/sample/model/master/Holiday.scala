package sample.model.master

import sample.context.{Entity, Dto}
import java.time.{LocalDate, LocalDateTime}
import sample.context.orm.SkinnyORMMapper
import sample.util.DateUtils
import scalikejdbc.jsr310._
import scalikejdbc._

/**
 * 休日マスタを表現します。
 */
case class Holiday(
  /** ID */
  id: Long,
  /** 休日区分 */
  category: String,
  /** 休日 */
  day: LocalDate,
  /** 休日名称 */
  name: String) extends Entity

object Holiday extends SkinnyORMMapper[Holiday] {
  override def extract(rs: WrappedResultSet, rn: ResultName[Holiday]): Holiday = autoConstruct(rs, rn)
  
  /** 休日マスタを取得します。 */
  def get(day: LocalDate)(implicit s: DBSession): Option[Holiday] =
    Holiday.withAlias(m => findBy(sqls.eq(m.day, day)))
  
  /** 休日マスタを登録します。 */
  def register(p: RegHoliday)(implicit s: DBSession): Unit = {
    deleteBy(sqls
      .eq(Holiday.column.category, p.category)
      .and.between(Holiday.column.day, LocalDate.ofYearDay(p.year, 1), DateUtils.dayTo(p.year)))
    p.list.foreach(v =>
      createWithAttributes('category -> p.category, 'day -> v.day, 'name -> v.name))
  }
    
}

case class RegHoliday(category: String = "default", year: Int, list: Seq[RegHolidayItem]) extends Dto

case class RegHolidayItem(day: LocalDate, name: String) extends Dto
