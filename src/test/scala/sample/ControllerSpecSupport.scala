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



/**
 * Springコンテナを用いたフルセットのWeb検証用途。
 * <p>Controllerに対するURL検証はこちらを利用して下さい。
 */
abstract class ControllerSpecSupport extends ContainerSpecSupport {
  protected val logger: Logger = LoggerFactory.getLogger("ControllerTest")
  
  @Autowired
  protected var wac: WebApplicationContext = _

  protected var mockMvc: MockMvc = _
  
  @Before
  def before(): Unit = {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
  }
  
  def url(path: String): String = s"${prefix}${path}"
  
  def prefix: String = "/"
  
  /** 指定したパスをGET実行して結果を取得します。(status: 200) */
  def performGet(path: String): MvcResult =
    mockMvc.perform(get(url(path))).andExpect(status().isOk()).andReturn()
    
  /** 指定したパスをGET実行して結果を取得します。(status: 400) */
  def performGetWarn(path: String): MvcResult =
    mockMvc.perform(get(url(path))).andExpect(status().isBadRequest()).andReturn()

  /** 指定したパスをPOST実行して結果を取得します。(status: 200) */
  def performPost(path: String): MvcResult =
    mockMvc.perform(post(url(path))).andExpect(status().isOk()).andReturn()
    
  /** 指定したパスをPOST実行して結果を取得します。(status: 400) */
  def performPostWarn(path: String): MvcResult =
    mockMvc.perform(post(url(path))).andExpect(status().isBadRequest()).andReturn()
}