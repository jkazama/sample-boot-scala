package sample.usecase

import sample.context.actor.Actor
import sample.ErrorKeys
import sample.ValidationException

object ServiceUtils {
  
  /** 匿名以外の利用者情報を返します。 */
  def actorUser(actor: Actor): Actor =
    if (actor.roleType.anonymous) throw ValidationException(ErrorKeys.Authentication) else actor
}