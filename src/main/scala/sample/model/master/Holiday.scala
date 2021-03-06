package sample.model.master

import java.time.{LocalDate, LocalDateTime}
import scalikejdbc.jsr310._
import scalikejdbc._
import sample.context.{Entity, Dto}
import sample.context.orm.SkinnyORMMapper
import sample.util.DateUtils

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
      .eq(Holiday.column.category, p.categoryStr)
      .and.between(Holiday.column.day, LocalDate.ofYearDay(p.year, 1), DateUtils.dayTo(p.year)))
    p.list.foreach(v =>
      createWithAttributes('category -> p.categoryStr, 'day -> v.day, 'name -> v.name))
  }
    
}

case class RegHoliday(category: Option[String] = None, year: Int, list: Seq[RegHolidayItem]) extends Dto {
  def categoryStr: String = category.getOrElse("default")
}

case class RegHolidayItem(day: LocalDate, name: String) extends Dto
