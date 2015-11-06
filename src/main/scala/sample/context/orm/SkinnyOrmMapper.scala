package sample.context.orm

import skinny.orm.SkinnyCRUDMapper
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc._

trait SkinnyORMMapper[Entity] extends SkinnyCRUDMapper[Entity] {
  override lazy val defaultAlias = createAlias(aliasName)
  def aliasName: String = getClass.getSimpleName.substring(0, 1).toLowerCase
  override def useAutoIncrementPrimaryKey = true
}