package sample.model.master

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sample._
import scalikejdbc._

@RunWith(classOf[JUnitRunner])
class StaffAuthoritySpec extends UnitSpecSupport {
  behavior of "社員権限管理"
  
  it should "権限を検索できる" in { implicit session =>
    StaffAuthority.createWithAttributes('staffId -> "sid", 'authority -> "ROLE_A")
    StaffAuthority.createWithAttributes('staffId -> "sid", 'authority -> "ROLE_B")
    StaffAuthority.createWithAttributes('staffId -> "else", 'authority -> "ROLE_C")
    StaffAuthority.findByStaffId("sid").size should be (2)
  }

}