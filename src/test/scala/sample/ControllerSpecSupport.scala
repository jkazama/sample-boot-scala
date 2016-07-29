package sample

import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet._
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.test.web.servlet.result.MockMvcResultMatchers._
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import sample.context.Timestamper
import sample.model.BusinessDayHandler
import sample.model.DataFixtures
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.util.UriComponentsBuilder
import org.hamcrest.Matcher
import org.springframework.web.util.UriComponents
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

/**
 * Spring コンテナを用いた Web 検証サポートクラス。
 * <p>Controller に対する URL 検証はこちらを利用して下さい。
 * <p>本クラスを継承したテストクラスを作成後、以下のアノテーションを付与してください。
 * <pre>
 *  {@literal @}RunWith(SpringRunner.class)
 *  {@literal @}WebMvcTest([テスト対象クラス].class)
 * </pre>
 * <p>{@literal @}WebMvcTest 利用時は標準で {@literal @}Component や {@literal @}Service 等の
 * コンポーネントはインスタンス化されないため、必要に応じて {@literal @}MockBean などを利用して代替するようにしてください。
 */
@ActiveProfiles(Array("testweb"))
abstract class ControllerSpecSupport {
  protected val logger: Logger = LoggerFactory.getLogger("ControllerTest")
  
  @Autowired
  protected var mvc: MockMvc = _
  
  protected val mockTime: Timestamper = Timestamper();
  protected val mockBusinessDay: BusinessDayHandler = new BusinessDayHandler { time = mockTime }
  protected val fixtures: DataFixtures =
    new DataFixtures {
      encoder = new BCryptPasswordEncoder()
      businessDay = mockBusinessDay
    }
  
  def uri(path: String): String = s"${prefix}${path}"
  
  def uriBuilder(path: String): UriComponentsBuilder =
    UriComponentsBuilder.fromUriString(uri(path));
  
  def prefix: String = "/"
  
  /** Get 要求を投げて結果を検証します。 */
  def performGet(path: String, expects: JsonExpects): ResultActions =
    performGet(uriBuilder(path).build(), expects)
  def performGet(uri: UriComponents, expects: JsonExpects): ResultActions =
    perform(
        get(uri.toUriString()).accept(MediaType.APPLICATION_JSON),
        expects.expects.toList)

  /** Get 要求 ( JSON ) を投げて結果を検証します。 */
  def performJsonGet(path: String, content: String, expects: JsonExpects): ResultActions =
    performJsonGet(uriBuilder(path).build(), content, expects)
  def performJsonGet(uri: UriComponents, content: String, expects: JsonExpects): ResultActions =
    perform(
        get(uri.toUriString()).contentType(MediaType.APPLICATION_JSON).content(content).accept(MediaType.APPLICATION_JSON),
        expects.expects.toList)

  /** Post 要求を投げて結果を検証します。 */
  def performPost(path: String, expects: JsonExpects): ResultActions =
    performPost(uriBuilder(path).build(), expects)
  def performPost(uri: UriComponents, expects: JsonExpects): ResultActions =
    perform(
        post(uri.toUriString()).accept(MediaType.APPLICATION_JSON),
        expects.expects.toList)

  /** Post 要求 ( JSON ) を投げて結果を検証します。 */
  def performJsonPost(path: String, content: String, expects: JsonExpects): ResultActions =
    performJsonPost(uriBuilder(path).build(), content, expects)
  def performJsonPost(uri: UriComponents, content: String, expects: JsonExpects): ResultActions =
    perform(
        post(uri.toUriString()).contentType(MediaType.APPLICATION_JSON).content(content).accept(MediaType.APPLICATION_JSON),
        expects.expects.toList)

  def perform(req: RequestBuilder, expects: Seq[ResultMatcher]): ResultActions = {
    var result = mvc.perform(req)
    expects.foreach(result.andExpect)
    result
  }
}

/** JSON 検証をビルダー形式で可能にします */
class JsonExpects {
  var expects = scala.collection.mutable.ListBuffer[ResultMatcher]();
  def value(key: String, expectedValue: Any): JsonExpects = {
    this.expects += jsonPath(key).value(expectedValue)
    this
  }
  def matcher[T](key: String, matcher: Matcher[T]): JsonExpects = {
    this.expects += jsonPath(key).value(matcher)
    this
  }
  def empty(key: String): JsonExpects = {
    this.expects += jsonPath(key).isEmpty()
    this
  }
  def notEmpty(key: String): JsonExpects = {
    this.expects += jsonPath(key).isNotEmpty()
    this
  }
  def array(key: String): JsonExpects = {
    this.expects += jsonPath(key).isArray()
    this
  }
  def map(key: String): JsonExpects = {
    this.expects += jsonPath(key).isMap()
    this
  }
}
object JsonExpects {
  // 200 OK
  def success(): JsonExpects = {
    var v = new JsonExpects()
    v.expects += status().isOk()
    v
  }
  // 400 Bad Request
  def failure(): JsonExpects = {
    var v = new JsonExpects()
    v.expects += status().isBadRequest()
    v
  }
}
