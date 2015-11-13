package sample.controller.admin

import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import sample.model.DataFixtures
import sample.util.DateUtils
import sample.ControllerSpecSupport
import org.junit.Test

//low: 簡易な正常系検証が中心
@RunWith(classOf[SpringJUnit4ClassRunner])
class AssetAdminControllerSpec extends ControllerSpecSupport {
 
  override def prefix = "/api/admin/asset"
  
  @Test
  def findCashInOut() = {
    tx { implicit session =>
      DataFixtures.saveCio(businessDay, "sample", "3000", true)
      DataFixtures.saveCio(businessDay, "sample", "8000", true)
    }
    // low: JSONの値検証は省略
    val day = DateUtils.dayFormat(dh.time.day)
    val query = s"updFromDay=${day}&updToDay=${day}"
    logger.info(performGet(s"/cio/?${query}").getResponse().getContentAsString())
  }

}