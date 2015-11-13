package sample.usecase

import sample._
import sample.context.actor.Actor

/**
 * Serviceで利用されるユーティリティ処理。
 */
trait ServiceUtils {
  
  /** 匿名以外の利用者情報を返します。 */
  def actorUser(actor: Actor): Actor =
    if (actor.roleType.anonymous) throw ValidationException(ErrorKeys.Authentication) else actor

}
object ServiceUtils extends ServiceUtils
