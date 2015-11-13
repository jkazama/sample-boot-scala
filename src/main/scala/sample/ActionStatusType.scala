package sample

import com.fasterxml.jackson.annotation.JsonValue

import sample.context.Enums

/**
 * 何らかの行為に関わる処理ステータス概念。
 */
sealed trait ActionStatusType {
  @JsonValue def value: String = this.toString()
  /** 完了済みのステータスの時はtrue */
  def isFinish: Boolean = ActionStatusType.finishTypes.contains(this)
  /** 未完了のステータス(処理中は含めない)の時はtrue */
  def isUnprocessing: Boolean = ActionStatusType.unprocessingTypes.contains(this)
  /** 未完了のステータス(処理中も含める)の時はtrue */
  def isUnprocessed: Boolean = ActionStatusType.unprocessedTypes.contains(this)
}
object ActionStatusType extends Enums[ActionStatusType] {
  /** 未処理 */
  case object UNPROCESSED extends ActionStatusType
  /** 処理中 */
  case object PROCESSING extends ActionStatusType
  /** 処理済 */
  case object PROCESSED extends ActionStatusType
  /** 取消 */
  case object CANCELLED extends ActionStatusType
  /** エラー */
  case object ERROR extends ActionStatusType

  override def values = List(UNPROCESSED, PROCESSING, PROCESSED, CANCELLED, ERROR)

  /** 完了済みのステータス一覧 */
  def finishTypes = List(PROCESSED, CANCELLED)
  def finishTypeValues: List[String] = finishTypes.map(_.value)
  /** 未完了のステータス一覧(処理中は含めない) */
  def unprocessingTypes = List(UNPROCESSED, ERROR)
  def unprocessingTypeValues: List[String] = unprocessingTypes.map(_.value)
  /** 未完了のステータス一覧(処理中も含める) */
  def unprocessedTypes = List(UNPROCESSED, PROCESSING, ERROR)
  def unprocessedTypeValues: List[String] = unprocessedTypes.map(_.value)
}
