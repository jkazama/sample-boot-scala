package sample.context.security

import scala.beans.BeanInfo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * Spring Securityに依存しないセキュリティ関連の設定定義を表現します。
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[SecurityProperties]))
@Order(org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfig {
  
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

/** セキュリティ関連の設定情報を表現します。 */
@BeanInfo
@ConfigurationProperties(prefix = "extension.security")
class SecurityProperties {
	/** Spring Security依存の認証/認可設定情報 */
	var auth: SecurityAuthProperties = new SecurityAuthProperties()
	/** CORS設定情報 */
	var cors: SecurityCorsProperties = new SecurityCorsProperties()
}

/** CORS設定情報を表現します。 */
@BeanInfo
class SecurityCorsProperties {
  var allowCredentials = true
  var allowedOrigin = "*"
  var allowedHeader = "*"
  var allowedMethod = "*"
  var maxAge: Long = 3600L
  var path = "/**"
}
