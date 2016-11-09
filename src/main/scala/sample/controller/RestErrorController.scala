package sample.controller

import org.springframework.boot.autoconfigure.web._
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired

/**
 * REST用の例外ハンドリングを行うController。
 * <p>application.ymlの"error.path"属性との組合せで有効化します。
 * あわせて"error.whitelabel.enabled: false"でwhitelabelを無効化しておく必要があります。
 * see ErrorMvcAutoConfiguration
 */
@RestController
class RestErrorController extends ErrorController {
  @Autowired
  var errorAttributes: ErrorAttributes = _
  override def getErrorPath() = "/api/error"
  
  @RequestMapping(Array("/api/error"))
  def error(request: HttpServletRequest): java.util.Map[String, Object] =
    errorAttributes.getErrorAttributes(
        new ServletRequestAttributes(request), false)

}
