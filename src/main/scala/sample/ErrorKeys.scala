package sample

/** 審査例外で用いるメッセージキー定数 */
trait ErrorKeys {
  /** サーバー側で問題が発生した可能性があります */
  val Exception = "error.Exception"
  /** 情報が見つかりませんでした */
  val EntityNotFound = "error.EntityNotFoundException"
  /** ログイン状態が有効ではありません */
  val Authentication = "error.Authentication"
  /** 対象機能の利用が認められていません */
  val AccessDenied = "error.AccessDeniedException"

  /** ログインに失敗しました */
  val Login = "error.login"
  /** 既に登録されているIDです */
  val DuplicateId = "error.duplicateId"

  /** 既に処理済の情報です */
  val ActionUnprocessing = "error.ActionStatusType.unprocessing"
}
object ErrorKeys extends ErrorKeys
