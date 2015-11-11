package sample.model.master

import sample.context.Entity
import sample.context.orm.SkinnyORMMapper
import scalikejdbc._

/**
 * 社員に割り当てられた権限を表現します。
 */
case class StaffAuthority(
  /** ID */
  id: Long,
  /** 社員ID */
  staffId: String,
  /** 権限名称。(「プリフィックスにROLE_」を付与してください) */
  authority: String) extends Entity

object StaffAuthority extends SkinnyORMMapper[StaffAuthority] {
  override def extract(rs: WrappedResultSet, rn: ResultName[StaffAuthority]): StaffAuthority = autoConstruct(rs, rn)
  
  /** 口座IDに紐付く権限一覧を返します。 */
  def findByStaffId(staffId: String)(implicit s: DBSession): List[StaffAuthority] =
    withAlias(m => findAllBy(sqls.eq(m.staffId, staffId)))
}