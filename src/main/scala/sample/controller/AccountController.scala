package sample.controller

import org.springframework.web.bind.annotation._
import org.springframework.beans.factory.annotation.Autowired
import sample._
import sample.context.security._
import sample.usecase.AccountService

/**
 * 口座に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/account"))
class AccountController extends ControllerSupport {

  @Autowired
  private var service: AccountService = _
  @Autowired
  private var securityProps: SecurityProperties = _
  
  /** ログイン状態を確認します。 */
  @RequestMapping(value = Array("/loginStatus"))
  def loginStatus: Boolean = true
  
  /** 口座ログイン情報を取得します。 */
  @RequestMapping(value = Array("/loginAccount"))
  def loadLoginAccount: LoginAccount =
    if (securityProps.auth.enabled) {
      SecurityActorFinder.actorDetails.map(details =>
        LoginAccount(details.actor.id, details.actor.name, details.authorityIds)
      ).getOrElse(throw ValidationException(ErrorKeys.Authentication))
    } else LoginAccount("sample", "sample", Seq()) // for dummy login
  
}

/** クライアント利用用途に絞ったパラメタ */
case class LoginAccount(id: String, name: String, authorities: Seq[String])
