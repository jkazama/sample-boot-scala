package sample.usecase

import org.springframework.boot.autoconfigure.condition._
import org.springframework.context.annotation.Bean

import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

import sample.ErrorKeys
import sample.context.security._
import sample.context.security.SecurityAuthConfig
import sample.util.ConvertUtils

/**
 * SpringSecurityのユーザアクセスコンポーネントを定義します。
 */
@Configuration
class SecurityService {
  
  /** 一般利用者情報を提供します。(see SecurityActorFinder) */
  @Bean
  @ConditionalOnBean(Array(classOf[SecurityAuthConfig]))
  def securityUserService(service: AccountService): SecurityUserService =
    new SecurityUserService {
      def loadUserByUsername(username: String): ActorDetails =
        Option(username).map(ConvertUtils.zenkakuToHan(_)).flatMap(loginId =>
          service.getLoginByLoginId(loginId).flatMap(login =>
            service.getAccount(login.id).map(account =>
              ActorDetails(account.actor, login.password, List(new SimpleGrantedAuthority("ROLE_USER")))
            )
          )
        ).getOrElse(throw new UsernameNotFoundException(ErrorKeys.Login))
    }
  /** 社内管理向けの利用者情報を提供します。(see SecurityActorFinder) */
  @Bean
  @ConditionalOnBean(Array(classOf[SecurityAuthConfig]))
  @ConditionalOnProperty(prefix = "extension.security.auth", name = Array("admin"), matchIfMissing = false)
  def securityAdminService(service: MasterAdminService): SecurityAdminService =
    new SecurityAdminService {
      def loadUserByUsername(username: String): ActorDetails =
        Option(username).map(ConvertUtils.zenkakuToHan(_)).flatMap(staffId =>
          service.getStaff(staffId).map(staff =>
            ActorDetails(staff.actor, staff.password,
              service.findStaffAuthority(staffId).map(sa => new SimpleGrantedAuthority(sa.authority))
              :+ new SimpleGrantedAuthority("ROLE_ADMIN"))
          )
        ).getOrElse(throw new UsernameNotFoundException(ErrorKeys.Login))    
    }
}