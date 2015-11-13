package sample.client

import scala.util.{Try, Success, Failure}
import java.net.URI
import org.apache.commons.io.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import sample.util._

/**
 * 単純なHTTP経由の実行検証。
 * <p>SpringがサポートするControllerSpecSupportでの検証で良いのですが、コンテナ立ち上げた後に叩く単純確認用に作りました。
 * <p>「extention.security.auth.enabled: true」の時は実際にログインして処理を行います。
 * falseの時はDummyLoginInterceptorによる擬似ログインが行われます。
 */
@RunWith(classOf[JUnit4])
class SampleClient {
  
  // 「extention.security.auth.admin: false」の時のみ利用可能です。
  @Test
  def 顧客向けユースケース検証: Unit = {
    val agent = new SimpleTestAgent()
    agent.login("sample", "sample")
    agent.post("振込出金依頼", "/asset/cio/withdraw?accountId=sample&currency=JPY&absAmount=200")
    agent.get("振込出金依頼未処理検索", "/asset/cio/unprocessedOut/")
  }
  
  // 「extention.security.auth.admin: true」の時のみ利用可能です。
  @Test
  def 社内向けユースケース検証: Unit = {
    val day = DateUtils.dayFormat(TimePoint().day)
    val agent = new SimpleTestAgent()
    agent.login("sample", "sample")
    agent.get("振込入出金依頼検索", s"/admin/asset/cio/?updFromDay=${day}&updToDay=${day}")
  }
  
  @Test
  def バッチ向けユースケース検証: Unit = {
    val fromDay = DateUtils.dayFormat(TimePoint().day.minusDays(1))
    val toDay = DateUtils.dayFormat(TimePoint().day.plusDays(3))
    val agent = new SimpleTestAgent();
    agent.post("営業日を進める(単純日回しのみ)", "/system/job/daily/processDay")
    agent.post("当営業日の出金依頼を締める", "/system/job/daily/closingCashOut")
    agent.post("入出金キャッシュフローを実現する(受渡日に残高へ反映)", "/system/job/daily/realizeCashflow")
    agent.get("イベントログを検索する", s"/admin/system/audit/event/?fromDay=${fromDay}&toDay=${toDay}")
  }

}

/** 単純なSession概念を持つHTTPエージェント */
class SimpleTestAgent {
  private val RootPath = "http://localhost:8080/api"
  private val factory = new SimpleClientHttpRequestFactory()
  private var sessionId: Option[String] = None;

  def path(urlPath: String): URI = new URI(s"${RootPath}${urlPath}")
  def dumpTitle(title: String): SimpleTestAgent = {
    println("------- " + title + "------- ")
    this
  }
  def dump(res: ClientHttpResponse): ClientHttpResponse = {
    println(s"status: ${res.getRawStatusCode()}, text: ${res.getStatusText()}")
    Try(println(IOUtils.toString(res.getBody()))) match {
      case Success(v) => // nothing.
      case Failure(e) => println(e.getMessage)
    }
    res
  }
  
  def get(title: String, urlPath: String): ClientHttpResponse = {
    dumpTitle(title).dump(request(urlPath, HttpMethod.GET).execute())
  }
  private def request(urlPath: String, method: HttpMethod): ClientHttpRequest = {
    val req = factory.createRequest(path(urlPath), method)
    sessionId.map((jsessionId) => req.getHeaders.add("Cookie", jsessionId))
    req
  }
  def post(title: String, urlPath: String): ClientHttpResponse =
    dumpTitle(title).dump(request(urlPath, HttpMethod.POST).execute())

  def login(loginId: String, password: String): SimpleTestAgent =
    post("ログイン", "/login?loginId=" + loginId + "&password=" + password) match {
      case res if res.getStatusCode == HttpStatus.OK =>
        val cookieStr = res.getHeaders().get("Set-Cookie").get(0)
        sessionId = Option(cookieStr.substring(0, cookieStr.indexOf(';')))
        this
      case res => this
    }
}
