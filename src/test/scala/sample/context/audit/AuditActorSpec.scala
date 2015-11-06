package sample.context.audit

import sample.UnitSpecSupport
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.time.LocalDate

@RunWith(classOf[JUnitRunner])
class AuditActorSpec extends UnitSpecSupport {

  behavior of "利用者監査"

  it should "利用者監査登録/検索" in { implicit session =>
    val idA = AuditActor.register(RegAuditActor("testA"))
    val idB = AuditActor.register(RegAuditActor("testB"))
    val idC = AuditActor.register(RegAuditActor("testC"))
    AuditActor.finish(idA)
    AuditActor.cancel(idB, "取消")
    AuditActor.error(idC, "例外")
    println(AuditActor.find(FindAuditActor(fromDay = LocalDate.now(), toDay = LocalDate.now())))
  }
}
