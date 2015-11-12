package sample.model.account

import org.springframework.security.crypto.password.PasswordEncoder
import scalikejdbc._
import sample._
import sample.context._
import sample.context.orm.SkinnyORMMapperWithIdStr

/**
 * 口座ログインを表現します。
 * low: サンプル用に必要最低限の項目だけ
 */
case class Login(
  /** 口座ID */
  id: String,
  /** ログインID */
  loginId: String,
  /** パスワード(暗号化済) */
  password: String) extends Entity

object Login extends SkinnyORMMapperWithIdStr[Login] {
  override def extract(rs: WrappedResultSet, rn: ResultName[Login]): Login = autoConstruct(rs, rn)

  /** ログイン情報を取得します。 */
  def getByLoginId(loginId: String)(implicit s: DBSession): Option[Login] =
    Login.withAlias(m => findBy(sqls.eq(m.loginId, loginId)))
  
  /** ログイン情報を取得します。 */
  def load(id: String)(implicit s: DBSession): Login =
    findById(id).getOrElse(throw ValidationException(ErrorKeys.EntityNotFound))

  /** ログイン情報を登録します。 */
  def register(encoder: PasswordEncoder, p: RegAccount)(implicit s: DBSession): String =
    Login.createWithAttributes(
      'id -> p.id, 'loginId -> p.id, 'password -> encoder.encode(p.plainPassword))
    
  /** ログインIDを変更します。 */
  def changeLoginId(id: String, loginId: String)(implicit s: DBSession): Unit =
    Login.updateById(id).withAttributes('loginId -> loginId)
 
  /** パスワードを変更します。 */
  def changePassword(id: String, encoder: PasswordEncoder, plainPassword: String)(implicit s: DBSession): Unit =
    Login.updateById(id).withAttributes('password -> encoder.encode(plainPassword))
    
}
