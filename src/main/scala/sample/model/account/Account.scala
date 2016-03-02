package sample.model.account

import org.springframework.security.crypto.password.PasswordEncoder

import com.fasterxml.jackson.annotation.JsonValue

import sample._
import sample.context._
import sample.context.actor.{ Actor, ActorRoleType }
import sample.context.orm.SkinnyORMMapperWithIdStr
import scalikejdbc._

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
  statusType: AccountStatusType) extends Entity {

  def actor: Actor = Actor(id, name, ActorRoleType.User)

  /** 口座に紐付くログイン情報を取得します。 */
  def loadLogin(implicit s: DBSession): Login = Login.load(id)
}

object Account extends AccountMapper {

  /** 有効な口座を取得します。 */
  def getActive(id: String)(implicit s: DBSession): Option[Account] =
    findById(id).filter(!_.statusType.inactive)
  
  /** 有効な口座を返します */
  def loadActive(id: String)(implicit s: DBSession): Account = findById(id) match {
    case Some(acc) if acc.statusType.inactive => throw ValidationException("error.Account.loadActive")
    case Some(acc) => acc
    case None => throw ValidationException(ErrorKeys.EntityNotFound)
  }
  
  /** 
   * 口座の登録を行います。
   * <p>ログイン情報も同時に登録されます。
   */
  def register(encoder: PasswordEncoder, p: RegAccount)(implicit s: DBSession): String = {
    Login.register(encoder, p)
    createWithAttributes('id -> p.id, 'name -> p.name, 'mail -> p.mail,
      'statusType -> AccountStatusType.Normal.value)
  }

  /** 口座を変更します。 */
  def change(id: String, p: ChgAccount)(implicit s: DBSession): Unit =
    updateById(id).withAttributes('name -> p.name, 'mail -> p.mail)
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
  def inactive: Boolean = (this == AccountStatusType.Withdrawal)
}
object AccountStatusType extends Enums[AccountStatusType] {
  /** 通常 */
  case object Normal extends AccountStatusType
  /** 退会 */
  case object Withdrawal extends AccountStatusType
  
  override def values = List(Normal, Withdrawal)
}

/** 登録パラメタ */
case class RegAccount(id: String, name: String, mail: String, plainPassword: String)

/** 変更パラメタ */
case class ChgAccount(name: String, mail: String)
