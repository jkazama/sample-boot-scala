package sample.context

/**
 * 列挙型概念を表現します。
 */
trait Enums[A] {
  def values: List[A]
  def withName(name: String): A = values.find(_.toString.toUpperCase == name.trim().toUpperCase).getOrElse { throw new IllegalArgumentException(s"指定するEnumは存在していません [$name]") }
}
/** 列挙型要素を表現します。 */
trait EnumSealed
