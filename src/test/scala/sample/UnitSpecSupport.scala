package sample

import org.scalatest.BeforeAndAfter
import org.scalatest.Finders
import org.scalatest.ShouldMatchers
import org.scalatest.fixture.FlatSpec
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import sample.context.DomainHelper
import sample.context.Timestamper
import sample.context.orm.SkinnyOrm
import sample.model.DataFixtures
import scalikejdbc.scalatest.AutoRollback
import java.time.Clock
import sample.context.actor.ActorSession
import sample.context.AppSettingHandler
import sample.context.AppSetting

trait UnitSpecSupport extends FlatSpec with AutoRollback with BeforeAndAfter with ShouldMatchers {

  val clock: Clock = Clock.systemDefaultZone()
  
  implicit val dh: DomainHelper =
    new DomainHelper {
      time = Timestamper(clock)
      actorSession = new ActorSession()
      settingHandler = MockAppSettingHandler()
    }

  val orm: SkinnyOrm = new SkinnyOrm()

  before {
    orm.initialize()
    DataFixtures.executeDdl()
  }

  after {
//    orm.destroy()
  }

}

case class MockAppSettingHandler(settingMap: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]()) extends AppSettingHandler { 
  override def setting(id: String): AppSetting =
    settingMap.get(id).map(v => AppSetting(id = id, value = v)).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))
  override def update(id: String, value: String): AppSettingHandler = {
    settingMap.getOrElseUpdate(id, value)
    this
  }
}
