package sample.context.orm

/**
 * ソート情報を表現します。
 * 複数件のソート情報(SortOrder)を内包します。
 */
case class Sort(orders: Seq[SortOrder]) {
  /** ソート条件を追加します。 */
  def add(order: SortOrder): Sort = copy(orders = orders :+ order)
  def +(order: SortOrder): Sort = add(order)
  /** ソート条件(昇順)を追加します。 */
  def asc(property: String): Sort = add(SortOrder.asc(property))
  /** ソート条件(降順)を追加します。 */
  def desc(property: String): Sort = add(SortOrder.desc(property))
  /** ソート条件が未指定だった際にソート順が上書きされます。 */
  def ifEmpty(emptyOrders: SortOrder*): Sort =
    if (orders.isEmpty && emptyOrders.nonEmpty) copy(orders = emptyOrders)
    else this
}
object Sort {
  /** 昇順でソート情報を返します。 */
  def ascBy(property: String): Sort = Sort(Seq()).asc(property)
  /** 降順でソート情報を返します。 */
  def descBy(property: String): Sort = Sort(Seq()).desc(property)
}

/** フィールド単位のソート情報を表現します。 */
case class SortOrder(property: String, ascending: Boolean) {
  def ascendingName: String = if (ascending) "asc" else "desc"
}
object SortOrder {
  def asc(property: String): SortOrder = SortOrder(property, true)
  def desc(property: String): SortOrder = SortOrder(property, false)
}
