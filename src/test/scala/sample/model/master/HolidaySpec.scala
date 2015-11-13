package sample.model.master

import java.time.LocalDate

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import sample._

@RunWith(classOf[JUnitRunner])
class HolidaySpec extends UnitSpecSupport {
  behavior of "休日管理"
  
  it should "休日を登録できる" in { implicit session =>
    Holiday.register(RegHoliday(year = 2015, list = Seq(
        RegHolidayItem(LocalDate.of(2015, 11, 3), "test"),
        RegHolidayItem(LocalDate.of(2015, 11, 23), "test"))))
    Holiday.findAll().size should be (2)
  }
  
}