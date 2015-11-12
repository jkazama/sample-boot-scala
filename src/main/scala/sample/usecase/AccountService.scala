package sample.usecase

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import sample.model.account.Login
import scalikejdbc.DBSession
import sample.model.account.Account

/**
 * 口座ドメインに対する顧客ユースケース処理。
 */
@Service
class AccountService extends ServiceSupport {
  
  /** ログイン情報を取得します。 */
  @Cacheable(Array("AccountService.getLoginByLoginId"))
  def getLoginByLoginId(loginId: String): Option[Login] =
    tx(implicit session => Login.getByLoginId(loginId))

  /** 有効な口座情報を取得します。 */
  @Cacheable(Array("AccountService.getAccount"))
  def getAccount(id: String): Option[Account] =
    tx(implicit session => Account.getActive(id))
    
}