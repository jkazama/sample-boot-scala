package sample.controller.system

import org.hamcrest.Matchers._
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import sample._
import sample.ControllerSpecSupport
import sample.model.DataFixtures
import sample.model.asset._

//low: 簡易な正常系検証が中心。100万保有のsampleを前提としてしまっています。
@RunWith(classOf[SpringJUnit4ClassRunner])
class JobControllerSpec extends ControllerSpecSupport {
 
  override def prefix = "/api/system/job"
  
  @Test
  def processDay() = {
    val day = businessDay.day
    val dayPlus1 = businessDay.day(1)
    val dayPlus2 = businessDay.day(2)
    assertThat(dh.time.day, is(day))
    performPost("/daily/processDay")
    assertThat(dh.time.day, is(dayPlus1))
    performPost("/daily/processDay")
    assertThat(dh.time.day, is(dayPlus2))
  }
  
  @Test
  def closingCashOut() = {
    // 当日発生の振込出金依頼を準備
    val co = tx { implicit session =>
      DataFixtures.saveCio(businessDay, "sample", "3000", true, Some(dh.time.day))
    }
    tx { implicit session =>
      assertThat(CashInOut.load(co.id).statusType.value, is(ActionStatusType.UNPROCESSED.value))
    }
    // 実行検証
    performPost("/daily/closingCashOut");
    tx { implicit session => 
      assertThat(CashInOut.load(co.id).statusType.value, is(ActionStatusType.PROCESSED.value))
    }
  }

  @Test
  def realizeCashflow() = {
    val dayMinus1 = businessDay.day(-1)
    val day = businessDay.day
    // 当日実現のキャッシュフローを準備
    val cf = tx { implicit session => 
      val saved = DataFixtures.saveCf("sample", "3000", dayMinus1, day)
      assertThat(Cashflow.load(saved.id).statusType.value, is(ActionStatusType.UNPROCESSED.value))
      assertThat(CashBalance.getOrNew("sample", "JPY").amount, is(BigDecimal("1000000.0000")))
      saved
    }
    // 実行検証
    performPost("/daily/realizeCashflow");
    tx { implicit session =>
      assertThat(Cashflow.load(cf.id).statusType.value, is(ActionStatusType.PROCESSED.value))
      assertThat(CashBalance.getOrNew("sample", "JPY").amount, is(BigDecimal("1003000.0000")))
    }

  }

}