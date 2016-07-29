package sample

import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import sample.context.security.SecurityProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.filter.CorsFilter
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.core.annotation.Order
import sample.context.security.SecurityConfigurer
import org.springframework.security.authentication.AuthenticationManager
import sample.context.security.SecurityProvider
import sample.context.security.SecurityEntryPoint
import sample.context.security.LoginHandler
import sample.context.security.SecurityActorFinder

/**
 * アプリケーションのセキュリティ定義を表現します。
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[SecurityProperties]))
class ApplicationSecurityConfig {
  
  /** パスワード用のハッシュ(BCrypt)エンコーダー。 */
  //low: きちんとやるのであれば、strengthやSecureRandom使うなど外部切り出し含めて検討してください
  @Bean
  def passwordEncoder(): PasswordEncoder = new BCryptPasswordEncoder();
  
  /** CORS全体適用 */
  @Bean
  @ConditionalOnProperty(prefix = "extension.security.cors", name = Array("enabled"), matchIfMissing = false)
  def corsFilter(props: SecurityProperties): CorsFilter = {
    val source = new UrlBasedCorsConfigurationSource()
    val config = new CorsConfiguration()
    config.setAllowCredentials(props.cors.allowCredentials);
    config.addAllowedOrigin(props.cors.allowedOrigin);
    config.addAllowedHeader(props.cors.allowedHeader);
    config.addAllowedMethod(props.cors.allowedMethod);
    config.setMaxAge(props.cors.maxAge);
    source.registerCorsConfiguration(props.cors.path, config);
    new CorsFilter(source);
  }
  
}

/** Spring Security を用いた API 認証/認可定義を表現します。 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(prefix = "extension.security.auth", name = Array("enabled"), matchIfMissing = true)
@Order(org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER)
class AuthSecurityConfig {
  
  /** Spring Security 全般の設定 ( 認証/認可 ) を定義します。 */
  @Bean
  @Order(org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER)
  def securityConfigurer(): SecurityConfigurer = new SecurityConfigurer();
  
  /** Spring Security のカスタム認証プロセス管理コンポーネント。 */
  @Bean
  def authenticationManager(): AuthenticationManager = securityConfigurer().authenticationManagerBean();
  
  /** Spring Security のカスタム認証プロバイダ。 */
  @Bean
  def securityProvider(): SecurityProvider = new SecurityProvider();
  
  /** Spring Security のカスタムエントリポイント。 */
  @Bean
  def securityEntryPoint(): SecurityEntryPoint = new SecurityEntryPoint();
  
  /** Spring Security におけるログイン/ログアウト時の振る舞いを拡張するHandler。 */
  @Bean
  def loginHandler(): LoginHandler = new LoginHandler();
  
  /** Spring Security で利用される認証/認可対象となるユーザ情報を提供します。 */
  @Bean
  def securityActorFinder(): SecurityActorFinder = new SecurityActorFinder();
 
}

