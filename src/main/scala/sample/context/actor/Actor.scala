package sample.context.actor

import java.util.Locale
import com.fasterxml.jackson.annotation.JsonValue
import sample.context.Dto
import sample.context.Enums
import sample.context.EnumSealed

/** ユースケースにおける利用者を表現します。 */
case class Actor(
  /** 利用者ID */
  id: String,
  /** 利用者名称 */
  name: String,
  /** 利用者が持つ{@link ActorRoleType} */
  roleType: ActorRoleType,
  /** 利用者が使用する{@link Locale} */
  locale: Locale,
  /** 利用者の接続チャネル名称 */
  var channel: Option[String],
  /** 利用者を特定する外部情報。(IPなど) */
  var source: Option[String]) extends Dto

object Actor {
  def apply(id: String, roleType: ActorRoleType): Actor = apply(id, id, roleType)
  def apply(id: String, name: String, roleType: ActorRoleType): Actor = Actor(id, name, roleType, Locale.getDefault(), None, None)
  
  /** 匿名利用者定数 */
  val Anonymous: Actor = Actor("unknown", ActorRoleType.ANONYMOUS)
  /** システム利用者定数 */
  val System: Actor = Actor("system", ActorRoleType.SYSTEM);
}

/** 利用者の役割を表現します。 */
sealed trait ActorRoleType extends EnumSealed {
  @JsonValue def value: String = this.toString()
  def anonymous: Boolean = this == ActorRoleType.ANONYMOUS
  def system: Boolean = this == ActorRoleType.SYSTEM
  def notSystem: Boolean = !system
}
object ActorRoleType extends Enums[ActorRoleType] {
  /** 匿名利用者(ID等の特定情報を持たない利用者) */
  case object ANONYMOUS extends ActorRoleType
  /** 利用者(主にBtoCの顧客, BtoB提供先社員) */
  case object USER extends ActorRoleType
  /** 内部利用者(主にBtoCの社員, BtoB提供元社員) */
  case object INTERNAL extends ActorRoleType
  /** システム管理者(ITシステム担当社員またはシステム管理会社の社員) */
  case object ADMINISTRATOR extends ActorRoleType
  /** システム(システム上の自動処理) */
  case object SYSTEM extends ActorRoleType

  override def values = List(ANONYMOUS, USER, INTERNAL, ADMINISTRATOR, SYSTEM)
}