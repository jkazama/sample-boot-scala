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
  case object Unprocessed extends ActionStatusType
  /** 処理中 */
  case object Processing extends ActionStatusType
  /** 処理済 */
  case object Processed extends ActionStatusType
  /** 取消 */
  case object Cancelled extends ActionStatusType
  /** エラー */
  case object Error extends ActionStatusType

  override def values = List(Unprocessed, Processing, Processed, Cancelled, Error)

  /** 完了済みのステータス一覧 */
  def finishTypes = List(Processed, Cancelled)
  def finishTypeValues: List[String] = finishTypes.map(_.value)
  /** 未完了のステータス一覧(処理中は含めない) */
  def unprocessingTypes = List(Unprocessed, Error)
  def unprocessingTypeValues: List[String] = unprocessingTypes.map(_.value)
  /** 未完了のステータス一覧(処理中も含める) */
  def unprocessedTypes = List(Unprocessed, Processing, Error)
  def unprocessedTypeValues: List[String] = unprocessedTypes.map(_.value)
}
