package sample.model.account

import sample.ValidationException
import scalikejdbc._
import sample.context.orm.SkinnyORMMapper
import sample.context.Entity
import sample.ErrorKeys

/**
 * 口座に紐づく金融機関口座を表現します。
 * <p>口座を相手方とする入出金で利用します。
 * low: サンプルなので支店や名称、名義といった本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 */
case class FiAccount(
  /** ID */
  id: Long,
  /** 口座ID */
  accountId: String,
  /** 利用用途カテゴリ */
  category: String,
  /** 通貨 */
  currency: String,
  /** 金融機関コード */
  fiCode: String,
  /** 金融機関口座ID */
  fiAccountId: String) extends Entity

object FiAccount extends SkinnyORMMapper[FiAccount] {
  override def extract(rs: WrappedResultSet, rn: ResultName[FiAccount]): FiAccount = autoConstruct(rs, rn)

  def load(accountId: String, category: String, currency: String)(implicit s: DBSession): FiAccount =
    withAlias { m =>
      findBy(
        sqls.eq(m.accountId, accountId)
        .and(sqls.eq(m.category, category))
        .and(sqls.eq(m.currency, currency)))
      .getOrElse(
        throw ValidationException(ErrorKeys.EntityNotFound))
    }
}
