package sample.context.orm

import org.junit.runner.RunWith
import org.scalatest.ShouldMatchers
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import sample.context.orm.SortOrder._

@RunWith(classOf[JUnitRunner])
class PaginationSpec extends WordSpec with ShouldMatchers {
 
  "ページング検証" should {
    "初期化" in {
      Pagination().sortIfEmpty(asc("test"), desc("test")).sort.get.orders.length should be (2)
      Pagination(Pagination(), 501).maxPage should be (6)
      Pagination(Pagination(), 500).maxPage should be (5)
      Pagination(Pagination(), 499).maxPage should be (5)
      Pagination(3, 20).firstResult should be (40)
    }
  }
}