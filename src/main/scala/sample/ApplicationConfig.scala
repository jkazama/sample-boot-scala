package sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import sample.context.Timestamper

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 * <p>case classで作成されたコンポーネントやコンストラクタ引数を取るものなどもこちらで定義されます。
 */
@Configuration
class ApplicationConfig {
  @Bean
  def timestamper(): Timestamper = Timestamper()
}

@Configuration
class WebMvcConfig extends WebMvcConfigurerAdapter {
  @Autowired
  private var message: MessageSource = _
  /** BeanValidationメッセージのUTF-8に対応したValidator。 */
  @Bean
  def validator(): LocalValidatorFactoryBean = {
    val factory = new LocalValidatorFactoryBean()
    factory.setValidationMessageSource(message)
    factory
  }
  /** 標準Validatorの差し替えをします。 */
  override def getValidator(): org.springframework.validation.Validator = {
    validator()
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
