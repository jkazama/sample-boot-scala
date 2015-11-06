package sample.context

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import sample.context.actor.ActorSession
import sample.context.actor.Actor
import scala.beans.BeanInfo

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 */
@Component
class DomainHelper {

  /** スレッドローカルスコープの利用者セッションを取得します。 */
	@Autowired
	var actorSession: ActorSession = _
	/** 日時ユーティリティを取得します。 */
	@Autowired
	var time: Timestamper = _
	@Autowired
	var settingHandler: AppSettingHandler = _
	
	/** ログイン中のユースケース利用者を取得します。 */
	def actor: Actor = actorSession.actor()

	/** アプリケーション設定情報を取得します。 */
	def setting(id: String): AppSetting = settingHandler.setting(id)
	
	/** アプリケーション設定情報を設定します。 */
	def settingSet(id: String, value: String): Unit = settingHandler.update(id, value)

}
