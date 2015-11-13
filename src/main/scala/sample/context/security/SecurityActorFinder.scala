package sample.context.security

import java.util.Collection

import scala.beans.BeanInfo
import scala.collection.JavaConversions._

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Lazy
import org.springframework.security.core._
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails._
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
import sample.context.actor.Actor

/**
 * Spring Securityで利用される認証/認可対象となるユーザ情報を提供します。
 */
@Component
@ConditionalOnBean(Array(classOf[SecurityAuthConfig]))
class SecurityActorFinder {
  @Autowired
  private var props: SecurityProperties = _
  @Autowired
  @Lazy
  private var userService: SecurityUserService = _
  @Autowired(required = false)
  @Lazy
  private var adminService: SecurityAdminService = _

  /** 現在のプロセス状態に応じたUserDetailServiceを返します。 */
  def detailsService: SecurityActorService =
    if (props.auth.admin)
      Option(adminService).getOrElse(
        throw new IllegalStateException("SecurityAdminServiceをコンテナへBean登録してください。"))
    else userService  
}
object SecurityActorFinder {
  
  /**
   * 現在有効な認証情報を返します。
   */
  def authentication: Option[Authentication] =
    Option(SecurityContextHolder.getContext().getAuthentication());

  /**
   * 現在有効な利用者認証情報を返します。
   * <p>ログイン中の利用者情報を取りたいときはこちらを利用してください。
   */
  def actorDetails: Option[ActorDetails] =
    authentication
      .filter(_.getDetails().isInstanceOf[ActorDetails])
      .map(_.getDetails().asInstanceOf[ActorDetails])

}

/**
 * 認証/認可で用いられるユーザ情報。
 * <p>プロジェクト固有にカスタマイズしています。
 */
case class ActorDetails(
  /** ログイン中の利用者情報 */
  actor: Actor,
  /** 認証パスワード(暗号化済) */
  password: String,
  /** 利用者の所有権限一覧 */
  authorities: Seq[GrantedAuthority]) extends UserDetails {
  
  //low: L/B経由をきちんと考えるならヘッダーもチェックすること
  def bindRequestInfo(request: HttpServletRequest): ActorDetails = {
    actor.source = Option(request.getRemoteAddr())
    this
  }

  override def getUsername(): String = actor.id
  override def getPassword(): String = password
  override def isAccountNonExpired(): Boolean = return true
  override def isAccountNonLocked(): Boolean = return true
  override def isCredentialsNonExpired(): Boolean = return true
  override def isEnabled(): Boolean = return true
  override def getAuthorities(): Collection[GrantedAuthority] = authorities
  def authorityIds: Seq[String] = authorities.map(_.getAuthority)
}

/** Actorに適合したUserDetailsService */
trait SecurityActorService extends UserDetailsService {
  /**
   * 与えられたログインIDを元に認証/認可対象のユーザ情報を返します。
   * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
   */
  override def loadUserByUsername(username: String): ActorDetails    
}

/** 一般利用者向けI/F */
trait SecurityUserService extends SecurityActorService
  
/** 管理者向けI/F */
trait SecurityAdminService extends SecurityActorService