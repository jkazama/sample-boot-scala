package sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health._
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.context.MessageSource
import org.springframework.context.annotation._
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sample.context.Timestamper
import sample.model.BusinessDayHandler
import sample.context.actor.ActorSession
import sample.context.ResourceBundleHandler
import sample.context.AppSettingHandler
import sample.context.audit.AuditHandler
import sample.context.audit.AuditPersister
import sample.context.lock.IdLockHandler
import sample.context.mail.MailHandler
import sample.context.report.ReportHandler
import sample.context.DomainHelper

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 * <p>case classで作成されたコンポーネントやコンストラクタ引数を取るものなどもこちらで定義されます。
 */
@Configuration
class ApplicationConfig {
  @Bean
  def timestamper(): Timestamper = Timestamper()
  @Bean
  def actorSession(): ActorSession = new ActorSession()
  @Bean
  def resourceBundleHandler(): ResourceBundleHandler = new ResourceBundleHandler();
  @Bean
  def appSettingHandler(): AppSettingHandler = new AppSettingHandler();
  @Bean
  def auditHandler(): AuditHandler = new AuditHandler();
  @Bean
  def auditPersister(): AuditPersister = new AuditPersister();
  @Bean
  def idLockHandler(): IdLockHandler = new IdLockHandler();
  @Bean
  def mailHandler(): MailHandler = new MailHandler();
  @Bean
  def reportHandler(): ReportHandler = new ReportHandler();
  @Bean
  def domainHelper(): DomainHelper = new DomainHelper();
}

@Configuration
class WebMVCConfig {

  /** BeanValidationメッセージのUTF-8に対応したValidator。 */
  @Bean
  def mvcValidator(message: MessageSource): LocalValidatorFactoryBean = {
    val factory = new LocalValidatorFactoryBean()
    factory.setValidationMessageSource(message)
    factory
  }

  /** Scalaの型に対応したObjectMapper */
  @Bean
  def scalaObjectMapper(): Jackson2ObjectMapperBuilder =
    Jackson2ObjectMapperBuilder.json()
      .autoDetectFields(true)
      .indentOutput(true)
      .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .modules(DefaultScalaModule)
      .modulesToInstall(classOf[JavaTimeModule])
}

/** 拡張ヘルスチェック定義を表現します。 */
@Configuration
class HealthCheckConfig {
  /** 営業日チェック */
  @Bean
  def dayIndicator(time: Timestamper, businessDay: BusinessDayHandler): HealthIndicator =
    new AbstractHealthIndicator {
      override def doHealthCheck(builder: Builder): Unit = {
        builder.up()
          .withDetail("day", businessDay.day)
          .withDetail("dayMinus1", businessDay.day(-1))
          .withDetail("dayPlus1", businessDay.day(1))
          .withDetail("dayPlus2", businessDay.day(2))
          .withDetail("dayPlus3", businessDay.day(3))
      }
    }
}
