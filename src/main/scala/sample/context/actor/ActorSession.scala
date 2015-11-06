package sample.context.actor

import org.springframework.stereotype.Component

/** スレッドローカルスコープの利用者セッション。 */
@Component
class ActorSession {
  val actorLocal = new ThreadLocal[Actor]()

	/** 利用者セッションへ利用者を紐付けます。 */
  def bind(actor: Actor): ActorSession = {
    actorLocal.set(actor)
    this
  }

	/** 利用者セッションを破棄します。 */
  def unbind(): ActorSession = {
    actorLocal.remove()
    this
  }

	/** 有効な利用者を返します。紐付けされていない時は匿名者が返されます。 */
  def actor(): Actor = Option(actorLocal.get()).getOrElse(Actor.Anonymous)
}