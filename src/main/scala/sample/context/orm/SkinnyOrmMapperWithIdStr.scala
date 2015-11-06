package sample.context.orm

import skinny.orm.SkinnyCRUDMapperWithId

trait SkinnyORMMapperWithIdStr[Entity] extends SkinnyCRUDMapperWithId[String, Entity] {
  override lazy val defaultAlias = createAlias(aliasName)
  def aliasName = getClass.getSimpleName.substring(0, 1).toLowerCase
  override def useExternalIdGenerator = true
  override def idToRawValue(id: String): String = id
  override def rawValueToId(rawValue: Any): String = rawValue.toString
}
