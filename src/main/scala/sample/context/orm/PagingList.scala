package sample.context.orm

import sample.context.Dto

/**
 * ページング一覧を表現します。
 */
case class PagingList[T](list: Seq[T], page: Pagination) extends Dto
