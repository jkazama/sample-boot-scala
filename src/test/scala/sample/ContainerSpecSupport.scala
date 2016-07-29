package sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.web.WebAppConfiguration
import sample.context.DomainHelper
import sample.model.BusinessDayHandler
import scalikejdbc._
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import sample.context.actor.ActorRoleType
import sample.context.actor.Actor
import sample.model.DataFixtures

/**
 * Springコンテナを用いたフルセットの検証用途。
 */
//low: メソッド毎にコンテナ初期化を望む時はDirtiesContextでClassMode.AFTER_EACH_TEST_METHODを利用
@SpringBootTest(classes = Array(classOf[Application]))
@ActiveProfiles(Array("test"))
abstract class ContainerSpecSupport {
  
  @Autowired
  protected implicit var dh: DomainHelper = _
  @Autowired
  protected var businessDay: BusinessDayHandler = _
  @Autowired
  protected var fixtures: DataFixtures = _
  
  /** 利用者として擬似ログインします */
  protected def loginUser(id: String): Unit =
    dh.actorSession.bind(Actor(id, ActorRoleType.User));
  
  /** 社内利用者として擬似ログインします */
  protected def loginInternal(id: String): Unit =
    dh.actorSession.bind(Actor(id, ActorRoleType.Internal));

  /** システム利用者として擬似ログインします */
  protected def loginSystem(): Unit =
    dh.actorSession.bind(Actor.System);
  
  /** トランザクション処理を実行します。 */
  protected def tx[T](callable: DBSession => T): T =
    DB.localTx(implicit session => callable(session))
    
}