package sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.web.WebAppConfiguration

import sample.context.DomainHelper
import sample.model.BusinessDayHandler
import scalikejdbc._

/**
 * Springコンテナを用いたフルセットの検証用途。
 */
//low: メソッド毎にコンテナ初期化を望む時はDirtiesContextでClassMode.AFTER_EACH_TEST_METHODを利用
@SpringApplicationConfiguration(classes = Array(classOf[Application]))
@WebAppConfiguration
@DirtiesContext
abstract class ContainerSpecSupport {
  
  @Autowired
  protected implicit var dh: DomainHelper = _
  @Autowired
  protected var businessDay: BusinessDayHandler = _
  
  /** トランザクション処理を実行します。 */
  protected def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))
    
}