package sample

import org.springframework.context.annotation.Configuration
import sample.context.orm.SkinnyOrm
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Bean

/**
 * アプリケーションのデータベース接続定義を表現します。
 */
@Configuration
class ApplicationDbConfig {
 
  @Bean
  @Primary
  def skinnyOrm(): SkinnyOrm = new SkinnyOrm();
  
}