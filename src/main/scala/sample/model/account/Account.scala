package sample.model.account

import sample.ValidationException
import sample.context._
import sample.context.orm.SkinnyORMMapperWithIdStr
import scalikejdbc._
import sample.ErrorKeys
import com.fasterxml.jackson.annotation.JsonValue
import sample.context.orm.SkinnyORMMapperWithIdStr


/**
 * 口座を表現します。
 * low: サンプル用に必要最低限の項目だけ
 */
case class Account(
  /** 口座ID */
  id: String,
  /** 口座名義 */
  name: String,
  /** メールアドレス */
  mail: String,
  /** 口座状態 */
  statusType: AccountStatusType) extends Entity

object Account extends AccountMapper {

  /** 有効な口座を返します */
  def loadActive(id: String)(implicit s: DBSession): Account = findById(id) match {
    case Some(acc) if acc.statusType.inactive => throw ValidationException("error.Account.loadActive")
    case Some(acc) => acc
    case None => throw ValidationException(ErrorKeys.EntityNotFound)
  }
}

trait AccountMapper extends SkinnyORMMapperWithIdStr[Account] {
  override def extract(rs: WrappedResultSet, n: ResultName[Account]) =
    Account(
      id = rs.string(n.id),
      name = rs.string(n.name),
      mail = rs.string(n.mail),
      statusType = AccountStatusType.withName(rs.string(n.statusType)))
}

/** 口座状態を表現します */
sealed trait AccountStatusType extends EnumSealed {
  @JsonValue def value: String = this.toString()
  def inactive: Boolean = (this == AccountStatusType.WITHDRAWAL)
}
object AccountStatusType extends Enums[AccountStatusType] {
  /** 通常 */
  case object NORMAL extends AccountStatusType
  /** 退会 */
  case object WITHDRAWAL extends AccountStatusType
  
  override def values = List(NORMAL, WITHDRAWAL)
}
