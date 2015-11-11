package sample.model.master

import sample.ValidationException
import scalikejdbc._
import sample.context.orm.SkinnyORMMapper
import sample.context.Entity
import sample.ErrorKeys

/**
 * サービス事業者の決済金融機関を表現します。
 * low: サンプルなので支店や名称、名義といったなど本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 */
case class SelfFiAccount(
  /** ID */
  id: Long,
  /** 利用用途カテゴリ */
  category: String,
  /** 通貨 */
  currency: String,
  /** 金融機関コード */
  fiCode: String,
  /** 金融機関口座ID */
  fiAccountId: String) extends Entity

object SelfFiAccount extends SkinnyORMMapper[SelfFiAccount] {
  override def extract(rs: WrappedResultSet, rn: ResultName[SelfFiAccount]): SelfFiAccount = autoConstruct(rs, rn)

  def load(category: String, currency: String)(implicit s: DBSession): SelfFiAccount =
    SelfFiAccount.withAlias { m =>
      findBy(sqls.eq(m.category, category).and(sqls.eq(m.currency, currency))).getOrElse(
        throw ValidationException(ErrorKeys.EntityNotFound))
    }
}

