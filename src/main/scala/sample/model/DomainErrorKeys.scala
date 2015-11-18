package sample.model

/** 汎用ドメインで用いるメッセージキー定数 */
trait DomainErrorKeys {
  /** マイナスを含めない数字を入力してください */
  val AbsAmountZero = "error.domain.AbsAmount.zero"
}
object DomainErrorKeys extends DomainErrorKeys
