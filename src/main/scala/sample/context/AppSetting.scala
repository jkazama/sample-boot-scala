package sample.context

import scala.BigDecimal

import sample._
import sample.context.orm.SkinnyORMMapperWithIdStr
import scalikejdbc._

/**
 * アプリケーション設定情報を表現します。
 * <p>事前に初期データが登録される事を前提とし、値の変更のみ許容します。
 */
case class AppSetting(
  /** 設定ID */
  id: String,
  /** 区分 */
  category: Option[String] = None,
  /** 概要 */
  outline: Option[String] = None,
  /** 値 */
  value: String) extends Entity {

  /** 設定情報値を取得します。 */
  def str(): String = value
  def str(defaultValue: String): String = Option(value).getOrElse(defaultValue)
  def intValue(): Int = value.toInt
  def intValue(defaultValue: Int): Int = Option(value).map(_.toInt).getOrElse(defaultValue)
  def longValue(): Long = value.toLong
  def longValue(defaultValue: Long): Long = Option(value).map(_.toLong).getOrElse(defaultValue)
  def bool(): Boolean = value.toBoolean
  def bool(defaultValue: Boolean): Boolean = Option(value).map(_.toBoolean).getOrElse(defaultValue)
  def decimal(): BigDecimal = BigDecimal(value)
  def decimal(defaultValue: BigDecimal): BigDecimal = Option(value).map(BigDecimal(_)).getOrElse(defaultValue)
}

object AppSetting extends SkinnyORMMapperWithIdStr[AppSetting] {
  override def extract(rs: WrappedResultSet, s: ResultName[AppSetting]): AppSetting = autoConstruct(rs, s)
  
  /** 設定情報を取得します。 */
  def get(id: String)(implicit s: DBSession = autoSession): Option[AppSetting] = findById(id)
  def load(id: String)(implicit s: DBSession = autoSession): AppSetting =
    get(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))

  /** アプリケーション設定情報を検索します。 */
  def find(p: FindAppSetting)(implicit s: DBSession = autoSession): Seq[AppSetting] =
    AppSetting.withAlias { m =>
      findAllBy(sqls.toAndConditionOpt(
        p.keyword.map(k =>
          sqls.like(m.id, p.likeKeyword)
          .or(sqls.like(m.category, p.likeKeyword))
          .or(sqls.like(m.outline, p.likeKeyword)))
      ).getOrElse(sqls.isNotNull(m.id)))
    }

  /** 設定情報値を設定します。 */
  def update(id: String, value: String)(implicit s: DBSession = autoSession): Unit =
    updateById(id).withAttributes('value -> value)

}

case class FindAppSetting(keyword: Option[String] = None) extends Dto {
  def likeKeyword = s"%${keyword.getOrElse("")}%"
}
