package sample.usecase

import org.springframework.cache.annotation.Cacheable
import sample.model.master.Staff
import org.springframework.stereotype.Service
import sample.model.master.StaffAuthority
import sample.model.master.RegHoliday
import sample.model.master.Holiday

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 */
@Service
class MasterAdminService extends ServiceSupport {

  /** 社員を取得します。 */
  @Cacheable(Array("MasterAdminService.getStaff"))
  def getStaff(id: String): Option[Staff] =
    tx(implicit session => Staff.get(id))

  /** 社員権限を取得します。 */
  @Cacheable(Array("MasterAdminService.findStaffAuthority"))
  def findStaffAuthority(staffId: String): List[StaffAuthority] =
    tx(implicit session => StaffAuthority.findByStaffId(staffId))

  def registerHoliday(p: RegHoliday): Unit =
    audit.audit("休日情報を登録する", () =>
      tx(implicit session => Holiday.register(p)))

}
