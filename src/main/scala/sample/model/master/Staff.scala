package sample.model.master

import sample.context.Entity
import sample.context.actor.{Actor, ActorRoleType}
import sample.context.orm.SkinnyORMMapperWithIdStr
import scalikejdbc._

/**
 * 社員を表現します。
 */
case class Staff(
  /** ID */
  id: String,
  /** 名前 */
  name: String,
  /** パスワード(暗号化済) */
  password: String) extends Entity {
  
  def actor: Actor = Actor(id, name, ActorRoleType.INTERNAL)
}

object Staff extends SkinnyORMMapperWithIdStr[Staff] {
  override def extract(rs: WrappedResultSet, rn: ResultName[Staff]): Staff = autoConstruct(rs, rn)
}
