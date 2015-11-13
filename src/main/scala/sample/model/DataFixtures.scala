package sample.model

import java.time._
import scala.beans.BeanInfo
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure._
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import sample._
import sample.context._
import sample.context.orm._
import sample.model.account._
import sample.model.asset._
import sample.model.master._
import sample.util._
import scalikejdbc._

/**
 * データ生成用のサポートコンポーネント。
 * テストや開発時の簡易マスタデータ生成を目的としているため本番での利用は想定していません。
 * low: 実際の開発では開発/テスト環境のみ有効となるよう細かなプロファイル指定が必要となります。
 */
@Component
@ConditionalOnProperty(prefix = "extension.datafixture", name = Array("enabled"), matchIfMissing = false)
@BeanInfo
class DataFixtures {
  
  @Autowired
  var encoder: PasswordEncoder = _
  @Autowired
  var businessDay: BusinessDayHandler = _

  @PostConstruct
  def initialize(): Unit = {
    import DataFixtures._
    executeDdl()
    // for sys
    DB.localTx { implicit session =>
      AppSetting.createWithAttributes(
        'id -> Timestamper.KeyDay, 'category -> Some("sysatem"),
        'outline -> Some("営業日"), 'value -> DateUtils.dayFormat(LocalDate.now()))
    }
    // for app
    DB.localTx { implicit session =>
      val ccy = "JPY"
        
      // 社員: admin (passも同様)
      saveStaff(encoder, "admin")
      
      // 自社金融機関
      saveSelfFiAcc(Remarks.CashOut, ccy)
      
      // 口座: sample (passも同様)
      val idSample = "sample"
      saveAcc(idSample, AccountStatusType.NORMAL)
      saveLogin(encoder, idSample)
      saveFiAcc(idSample, Remarks.CashOut, ccy)
      saveCb(idSample, businessDay.day, ccy, "1000000")
    }
  }
}

object DataFixtures extends DdlExecutor {

  // account
  
  /** 口座の簡易生成 */
  def saveAcc(id: String, statusType: AccountStatusType)(implicit session: DBSession): Account =
    Account.findById(Account.createWithAttributes(
      'id -> id, 'name -> id, 'mail -> "hoge@example.com", 'statusType -> statusType.value)).get

  def saveLogin(encoder: PasswordEncoder, id: String)(implicit session: DBSession): Login =
    Login.findById(Login.createWithAttributes(
      'id -> id, 'loginId -> id, 'password -> encoder.encode(id))).get
      
  /** 口座に紐付く金融機関口座の簡易生成 */
  def saveFiAcc(accountId: String, category: String, currency: String)(implicit session: DBSession): FiAccount =
    FiAccount.findById(FiAccount.createWithAttributes(
      'accountId -> accountId, 'category -> category, 'currency -> currency,
      'fiCode -> s"$category-$currency", 'fiAccountId -> s"FI$accountId")).get

  // asset
  
  /** 口座残高の簡易生成 */
  def saveCb(accountId: String, baseDay: LocalDate, currency: String, amount: String)(implicit session: DBSession): CashBalance =
    CashBalance.findById(CashBalance.createWithAttributes(
      'accountId -> accountId, 'baseDay -> baseDay, 'currency -> currency,
      'amount -> BigDecimal(amount), 'updateDate -> LocalDateTime.now)).get

  /** キャッシュフローの簡易生成(未処理) */
  def saveCf(accountId: String, amount: String, eventDay: LocalDate, valueDay: LocalDate, statusType: ActionStatusType = ActionStatusType.UNPROCESSED)(implicit session: DBSession): Cashflow =
    Cashflow.findById(Cashflow.createWithAttributes(
      'accountId -> accountId, 'currency -> "JPY", 'amount -> BigDecimal(amount),
      'cashflowType -> CashflowType.CashIn.value, 'remark -> Remarks.CashIn,
      'eventDay -> eventDay, 'eventDate -> LocalDateTime.now, 'valueDay -> valueDay,
      'statusType -> statusType.value)).get
  
  /** 振込入出金依頼の簡易生成 [発生日(T+1)/受渡日(T+3)]。（発生日指定時は発生日 = 受渡日） */
  def saveCio(businessDay: BusinessDayHandler,
      accountId: String, absAmount: String, withdrawal: Boolean, eventDay: Option[LocalDate] = None)(implicit session: DBSession): CashInOut =
    CashInOut.findById(CashInOut.createWithAttributes(
      'accountId -> accountId, 'currency -> "JPY", 'absAmount -> BigDecimal(absAmount),
      'withdrawal -> withdrawal, 'requestDay -> businessDay.day, 'requestDate -> LocalDateTime.now,
      'eventDay -> eventDay.getOrElse(businessDay.day(1)),
      'valueDay -> eventDay.getOrElse(businessDay.day(3)),
      'targetFiCode -> "tFiCode", 'targetFiAccountId -> "tFiAccId",
      'selfFiCode -> "sFiCode", 'selfFiAccountId -> "sFiAccId",
      'statusType -> ActionStatusType.UNPROCESSED.value, 'updateDate -> LocalDateTime.now)).get

  // master

  /** 社員の簡易生成 */
  def saveStaff(encoder: PasswordEncoder, id: String)(implicit session: DBSession): Staff =
    Staff.findById(Staff.createWithAttributes(
      'id -> id, 'name -> id, 'password -> encoder.encode(id))).get

  /** 自社金融機関口座の簡易生成 */
  def saveSelfFiAcc(category: String, currency: String)(implicit session: DBSession): SelfFiAccount =
    SelfFiAccount.findById(SelfFiAccount.createWithAttributes(
      'category -> category,
      'currency -> currency,
      'fiCode -> s"${category}-${currency}",
      'fiAccountId -> "xxxxxx")).get
  
}

trait DdlExecutor {
  def executeDdl() = DB autoCommit { implicit session =>
    SQL("""
      drop table app_setting if exists;
      drop table audit_actor if exists;
      drop table audit_event if exists;
      create table app_setting (id varchar(120) not null, category varchar(60), outline varchar(1300), value varchar(1300), primary key (id));
      create table audit_actor (id bigint auto_increment, actor_id varchar(32) not null, category varchar(30) not null, end_date timestamp, error_reason varchar(400), message varchar(255), role_type varchar(255) not null, source varchar(255), start_date timestamp not null, status_type varchar(255) not null, time bigint, primary key (id));
      create table audit_event (id bigint auto_increment, category varchar(255), end_date timestamp, error_reason varchar(255), message varchar(255), start_date timestamp not null, status_type varchar(255), time bigint, primary key (id));
      drop table account if exists;
      drop table cash_balance if exists;
      drop table cashflow if exists;
      drop table cash_in_out if exists;
      drop table fi_account if exists;
      drop table holiday if exists;
      drop table login if exists;
      drop table self_fi_account if exists;
      drop table staff if exists;
      drop table staff_authority if exists;
      create table account (id varchar(32) not null, mail varchar(256) not null, name varchar(30) not null, status_type varchar(255) not null, primary key (id));
      create table cash_balance (id bigint auto_increment, account_id varchar(32) not null, amount decimal(20,4) not null, base_day date not null, currency varchar(3) not null, update_date timestamp not null, primary key (id));
      create table cashflow (id bigint auto_increment, account_id varchar(32) not null, amount decimal(20,4) not null, cashflow_type varchar(255) not null, currency varchar(3) not null, event_date timestamp not null, event_day date not null, remark varchar(30) not null, status_type varchar(255) not null, value_day date not null, primary key (id));
      create table cash_in_out (id bigint auto_increment, abs_amount decimal(20,4) not null, account_id varchar(32) not null, cashflow_id bigint, currency varchar(3) not null, event_day date not null, request_date timestamp not null, request_day date not null, self_fi_account_id varchar(32) not null, self_fi_code varchar(32) not null, status_type varchar(255) not null, target_fi_account_id varchar(32) not null, target_fi_code varchar(32) not null, value_day date not null, withdrawal boolean not null, update_date timestamp not null, primary key (id));
      create table fi_account (id bigint auto_increment, account_id varchar(32) not null, category varchar(30) not null, currency varchar(3) not null, fi_account_id varchar(32) not null, fi_code varchar(32) not null, primary key (id));
      create table holiday (id bigint auto_increment, category varchar(30) not null, day date not null, name varchar(40) not null, primary key (id));
      create table login (id varchar(32) not null, login_id varchar(255), password varchar(256) not null, primary key (id));
      create table self_fi_account (id bigint auto_increment, category varchar(30) not null, currency varchar(3) not null, fi_account_id varchar(32) not null, fi_code varchar(32) not null, primary key (id));
      create table staff (id varchar(32) not null, name varchar(30) not null, password varchar(256) not null, primary key (id));
      create table staff_authority (id bigint auto_increment, authority varchar(30) not null, staff_id varchar(32) not null, primary key (id));
    """).execute.apply()
  }
}
