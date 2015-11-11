package sample.context

import java.util.Locale
import java.util.ResourceBundle

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Component

/**
 * ResourceBundleに対する簡易アクセスを提供します。
 * <p>本コンポーネントはAPI経由でのラベル一覧の提供等、i18n用途のメッセージプロパティで利用してください。
 * <p>ResourceBundleは単純な文字列変換を目的とする標準のMessageSourceとは異なる特性(リスト概念)を
 * 持つため、別インスタンスでの管理としています。
 * （spring.messageとは別に指定[extension.messages]する必要があるので注意してください）
 */
@Component
@ConfigurationProperties(prefix = "extension.messages")
class ResourceBundleHandler {
  
  @BeanProperty
  var encoding: String = "UTF-8"
  val factory: ResourceBundleFactory = new ResourceBundleFactory()
  val bundleMap: scala.collection.mutable.Map[String, ResourceBundle] = scala.collection.mutable.Map()
  
  def get(basename: String): ResourceBundle = get(basename, Locale.getDefault)
  def get(basename: String, locale: Locale): ResourceBundle =
    this.synchronized(
      bundleMap.getOrElseUpdate(keyname(basename, locale),
          factory.create(basename, locale, encoding))
    )
  private def keyname(basename: String, locale: Locale): String = s"${basename}_${locale.toLanguageTag()}"
  
  /**
   * 指定されたメッセージソースのラベルキー、値のMapを返します。
   * <p>basenameに拡張子(.properties)を含める必要はありません。
   */
  def labels(basename: String): Map[String, String] =
    labels(basename, Locale.getDefault)
  def labels(basename: String, locale: Locale): Map[String, String] = {
    val bundle = get(basename)
    bundle.keySet().map(key => (key, bundle.getString(key))).toMap
  }
    

}

/**
 * SpringのMessageSource経由でResourceBundleを取得するFactory。
 * <p>プロパティファイルのエンコーディング指定を可能にしています。
 */
class ResourceBundleFactory extends ResourceBundleMessageSource {
  def create(basename: String, locale: Locale, encoding: String): ResourceBundle = {
    this.setDefaultEncoding(encoding)
    Option(getResourceBundle(basename, locale)).getOrElse(
        throw new IllegalArgumentException("指定されたbasenameのリソースファイルは見つかりませんでした。[]"))
  }
}
