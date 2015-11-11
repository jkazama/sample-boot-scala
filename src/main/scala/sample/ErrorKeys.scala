package sample

/** 審査例外で用いるメッセージキー定数 */
trait ErrorKeys {
  val Exception = "error.Exception"
  val EntityNotFound = "error.EntityNotFoundException"
  val Authentication = "error.Authentication"
  val AccessDenied = "error.AccessDeniedException"

  val Login = "error.login"
  val DuplicateId = "error.duplicateId"
}
object ErrorKeys extends ErrorKeys
