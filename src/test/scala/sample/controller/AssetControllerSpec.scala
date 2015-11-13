package sample.controller

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import sample.ControllerSpecSupport
import scalikejdbc.DB
import sample.model.DataFixtures

//low: 簡易な正常系検証が中心
@RunWith(classOf[SpringJUnit4ClassRunner])
class AssetControllerSpec extends ControllerSpecSupport {
 
  override def prefix = "/api/asset"
  
  @Test
  def findUnprocessedCashOut() = {
    tx { implicit session =>
      DataFixtures.saveCio(businessDay, "sample", "3000", true)
      DataFixtures.saveCio(businessDay, "sample", "8000", true)
    }
    // low: JSONの値検証は省略
    logger.info(performGet("/cio/unprocessedOut/").getResponse().getContentAsString())
  }
  
  @Test
  def withdraw() = {
    val query = "accountId=sample&currency=JPY&absAmount=1000"
    // low: JSONの値検証は省略
    logger.info(performPost(s"/cio/withdraw?${query}").getResponse().getContentAsString())
  }

}