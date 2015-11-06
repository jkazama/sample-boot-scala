package sample.context.orm

import org.scalatest.ShouldMatchers
import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SortSpec extends WordSpec with ShouldMatchers {
 
  "ソート検証" should {
    "初期化" in {
      Sort(List(SortOrder.asc("a"), SortOrder.asc("b"))).orders.nonEmpty should be (true)
      Sort.ascBy("a").orders.nonEmpty should be (true)
      Sort.descBy("a").orders.nonEmpty should be (true)
    }
  }
}