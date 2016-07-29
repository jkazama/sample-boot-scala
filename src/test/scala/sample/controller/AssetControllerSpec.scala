package sample.controller

import org.mockito.BDDMockito._
import org.junit.Test
import org.junit.runner.RunWith
import sample.ControllerSpecSupport
import scalikejdbc.DB
import sample.model.DataFixtures
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import sample.usecase.AssetService
import sample.JsonExpects
import sample.model.asset.CashInOut
import sample.ActionStatusType

//low: 簡易な正常系検証が中心 ( 現状うまく起動できていないのでコメントアウト )
//@RunWith(classOf[SpringRunner])
@WebMvcTest(Array(classOf[AssetController]))
class AssetControllerSpec extends ControllerSpecSupport {
 
  @MockBean
  var service: AssetService = _
  
  override def prefix = "/api/asset"
  
  @Test
  def 未処理の振込依頼情報を検索します() = {
    given(service.findUnprocessedCashOut).willReturn(resultCashOuts())
    performGet("/cio/unprocessedOut/",
        JsonExpects.success()
            .value("$[0].currency", "JPY")
            .value("$[0].absAmount", 3000)
            .value("$[1].absAmount", 4000));
  }

  def resultCashOuts(): List[CashInOut] = List(cio("3000"), cio("4000"))
  def cio(absAmount: String): CashInOut =
    CashInOut(0, "sample", "JPY", BigDecimal(absAmount), true, null, null, null, null, null, null, null, null, ActionStatusType.Unprocessed, null, null)

}