package sample.context.security

import scala.beans.BeanProperty

import org.springframework.boot.context.properties._
import java.beans.SimpleBeanInfo



/** セキュリティ関連の設定情報を表現します。 */
@SimpleBeanInfo
@ConfigurationProperties(prefix = "extension.security")
class SecurityProperties {
  /** Spring Security依存の認証/認可設定情報 */
  @BeanProperty
  var auth: SecurityAuthProperties = new SecurityAuthProperties()
  /** CORS設定情報 */
  @BeanProperty
  var cors: SecurityCorsProperties = new SecurityCorsProperties()
}

/** Spring Securityに対する拡張設定情報。(ScurityConfig#SecurityPropertiesによって管理されています) */
@SimpleBeanInfo
class SecurityAuthProperties {
  /** リクエスト時のログインIDを取得するキー */
  @BeanProperty
  var loginKey = "loginId"
  /** リクエスト時のパスワードを取得するキー */
  @BeanProperty
  var passwordKey = "password"
  /** 認証対象パス */
  @BeanProperty
  var path = Array("/api/**")
  /** 認証対象パス(管理者向け) */
  @BeanProperty
  var pathAdmin = Array("/api/admin/**")
  /** 認証除外パス(認証対象からの除外) */
  @BeanProperty
  var excludesPath = Array("/api/system/job/**")
  /** 認証無視パス(フィルタ未適用の認証未考慮、静的リソース等) */
  @BeanProperty
  var ignorePath = Array("/css/**", "/js/**", "/img/**", "/**/favicon.ico")
  /** ログインAPIパス */
  @BeanProperty
  var loginPath = "/api/login"
  /** ログアウトAPIパス */
  @BeanProperty
  var logoutPath = "/api/logout"
  /** 一人が同時利用可能な最大セッション数 */
  @BeanProperty
  var maximumSessions: Int = 2
  /**
   * 社員向けモードの時はtrue。
   * <p>ログインパスは同じですが、ログイン処理の取り扱いが切り替わります。
   * <ul>
   * <li>true: SecurityUserService
   * <li>false: SecurityAdminService
   * </ul> 
   */
  @BeanProperty
  var admin = false;
  /** 認証が有効な時はtrue */
  @BeanProperty
  var enabled = true
}

/** CORS設定情報を表現します。 */
@SimpleBeanInfo
class SecurityCorsProperties {
  @BeanProperty
  var allowCredentials = true
  @BeanProperty
  var allowedOrigin = "*"
  @BeanProperty
  var allowedHeader = "*"
  @BeanProperty
  var allowedMethod = "*"
  @BeanProperty
  var maxAge: Long = 3600L
  @BeanProperty
  var path = "/**"
}
