package sample.context.orm

import org.junit.runner.RunWith

import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SortSpec extends WordSpec with Matchers {
 
  "ソート検証" should {
    "初期化" in {
      Sort(List(SortOrder.asc("a"), SortOrder.asc("b"))).orders.nonEmpty should be (true)
      Sort.ascBy("a").orders.nonEmpty should be (true)
      Sort.descBy("a").orders.nonEmpty should be (true)
    }
  }
}