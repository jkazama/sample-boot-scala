package sample.controller

import scala.beans.BeanInfo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid
import sample.model.constraints.AbsAmount
import sample.model.constraints.Name

/**
 * 資産に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping(Array("/api/asset"))
class AssetController extends ControllerSupport {
  
  @RequestMapping(value = Array("/hello"))
  def hello(): String = "Hello"
  
  @RequestMapping(value = Array("/labels"))
  def labels(): ResponseEntity[Map[String, String]] =
    result(labels("messages-validation"))

  @RequestMapping(value = Array("/check"))
  def check(@Valid p: Check): String = "Hello"
  
  @RequestMapping(value = Array("/op"))
  def option(p: Op): Op = new Op()
}

@BeanInfo
class Op {
  var ok: Option[String] = Some("ok")
  var ng: Option[String] = None
}

@BeanInfo
class Check {
  @Name
  var name: String = _
  @AbsAmount
  var amount: java.math.BigDecimal= _
}

trait AssetControllerDto {
  
}
