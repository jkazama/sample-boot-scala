package sample.model.master

import scalikejdbc._
import sample.context._
import sample.context.actor.{Actor, ActorRoleType}
import sample.context.orm.SkinnyORMMapperWithIdStr

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
  
  def actor: Actor = Actor(id, name, ActorRoleType.Internal)
}

object Staff extends SkinnyORMMapperWithIdStr[Staff] {
  override def extract(rs: WrappedResultSet, rn: ResultName[Staff]): Staff = autoConstruct(rs, rn)
  
  def get(id: String)(implicit s: DBSession): Option[Staff] = findById(id)
}
