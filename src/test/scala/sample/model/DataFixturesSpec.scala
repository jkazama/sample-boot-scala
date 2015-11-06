package sample.model

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, ShouldMatchers, WordSpec}
import sample.context.orm.SkinnyOrm

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class DataFixturesSpec extends WordSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {

  val fixtures: DataFixtures = new DataFixtures()
  val skinny: SkinnyOrm = new SkinnyOrm()

  before {
    skinny.initialize()
  }

  after {
    skinny.destroy()
  }

  "hello" should {
    "Hello" in {
      fixtures.initialize()
      "a" should be ("a")
    }
  }

}
