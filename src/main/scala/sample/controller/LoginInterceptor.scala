package sample.controller

import org.aspectj.lang.annotation._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

import sample.context.actor._
import sample.context.security.SecurityAuthConfig

/**
 * Spring Securityの設定状況に応じてスレッドローカルへ利用者を紐付けるAOPInterceptor。
 */
@Aspect
@Configuration
class LoginInterceptor {
  @Autowired
  private var session: ActorSession = _
  
  @Before("execution(* *..controller.system.*Controller.*(..))")
  def bindSystem() = session.bind(Actor.System)
  @After("execution(* *..controller..*Controller.*(..))")
  def unbind() = session.unbind()
}

/**
 * セキュリティの認証設定(extension.security.auth.enabled)が無効時のみ有効される擬似ログイン処理。
 * <p>開発時のみ利用してください。
 */
@Aspect
@Component
@ConditionalOnMissingBean(Array(classOf[SecurityAuthConfig]))
class DummyLoginInterceptor {
  @Autowired
  private var session: ActorSession = _
  
  @Before("execution(* *..controller.*Controller.*(..))")
  def bindUser() = session.bind(Actor("sample", ActorRoleType.User)) 
  
  @Before("execution(* *..controller.admin.*Controller.*(..))")
  def bindAdmin() = session.bind(Actor("admin", ActorRoleType.Internal));
}
