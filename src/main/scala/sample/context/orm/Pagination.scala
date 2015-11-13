package sample.context.orm

import scala.math.BigDecimal._
import scala.math.BigDecimal.RoundingMode

import sample.context.Dto
import sample.util.Calculator

/**
 * ページング情報を表現します。
 */
case class Pagination(
  /** ページ数(1開始) */
  page: Int,
  /** ページあたりの件数 */
  size: Int,
  /** トータル件数 */
  total: Option[Long],
  /** トータル件数算出を無視するか */
  ignoreTotal: Boolean,
  /** ソート条件 */
  sort: Option[Sort]) extends Dto {
  
  /** カウント算出を無効化します。 */
  def enableIgnoreTotal(): Pagination = copy(ignoreTotal = true)
  /** ソート指定が未指定の時は与えたソート条件で上書きします。 */
  def sortIfEmpty(orders: SortOrder*): Pagination =
    copy(sort = Some(
     sort
      .map(s => if (s.orders.nonEmpty) s.ifEmpty(orders: _*) else s)
      .getOrElse(Sort(orders))
    ))
  /** 最大ページ数を返します。total設定時のみ適切な値が返されます。 */
  def maxPage: Int = total.map(t => (Calculator(t, 0, RoundingMode.UP) / size).int).getOrElse(0)
  /** 開始件数を返します。 */
  def firstResult: Int = (page - 1) * size
}
object Pagination {
  val defaultSize: Int = 100
  def apply(): Pagination = apply(1)
  def apply(page: Int): Pagination = apply(page, defaultSize)
  def apply(page: Int, size: Int): Pagination = apply(page, size, null)
  def apply(page: Int, size: Int, sort: Sort): Pagination = Pagination(page, size, None, false, Option(sort))
  def apply(req: Pagination, total: Long): Pagination = apply(req.page, req.size, Some(total), false, req.sort)
}
