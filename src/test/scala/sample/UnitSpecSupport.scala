package sample

import java.time.Clock

import org.scalatest._
import org.scalatest.fixture.FlatSpec
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

import sample.context._
import sample.context.actor.ActorSession
import sample.context.orm.SkinnyOrm
import sample.model._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

abstract class UnitSpecSupport extends FlatSpec with AutoRollback with BeforeAndAfter with Matchers {

  val clock: Clock = Clock.systemDefaultZone()
  
  implicit val dh: DomainHelper =
    new DomainHelper {
      time = Timestamper(clock)
      actorSession = new ActorSession()
      settingHandler = MockAppSettingHandler()
    }
  
  val businessDay: BusinessDayHandler =
    new BusinessDayHandler {
      time = dh.time
      holidayAccessor = new HolidayAccessor
    }
  val encoder: PasswordEncoder = new BCryptPasswordEncoder()

  val orm: SkinnyOrm = new SkinnyOrm()

  before {
    orm.initialize()
    DataFixtures.executeDdl()
    DB.localTx(postBefore(_))
  }
  protected def postBefore: DBSession => Any = {implicit s: DBSession => Unit}

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
