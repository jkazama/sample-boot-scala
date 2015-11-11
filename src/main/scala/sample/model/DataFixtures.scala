package sample.model

import javax.annotation.PostConstruct
import org.springframework.stereotype.Component
import sample.model.account.{FiAccount, AccountStatusType, Account}
import scalikejdbc._

/**
 * データ生成用のサポートコンポーネント。
 * テストや開発時の簡易マスタデータ生成を目的としているため本番での利用は想定していません。
 * low: 実際の開発では開発/テスト環境のみ有効となるよう細かなプロファイル指定が必要となります。
 */
@Component
class DataFixtures {

  @PostConstruct
  def initialize(): Unit = {
    DataFixtures.executeDdl()
    DB.localTx { implicit session =>
      Account.createWithAttributes('id -> "sample", 'name -> "サンプル",
        'mail -> "a@a.com", 'statusType -> AccountStatusType.NORMAL)
    }
  }

}

object DataFixtures extends DdlExecutor {

  /** 口座の簡易生成 */
  def saveAcc(id: String, statusType: AccountStatusType)(implicit session: DBSession): Account =
    Account.findById(Account.createWithAttributes(
      'id -> id, 'name -> id, 'mail -> "hoge@example.com", 'statusType -> statusType.value)).get

  /** 口座に紐付く金融機関口座の簡易生成 */
  def saveFiAcc(accountId: String, category: String, currency: String)(implicit session: DBSession): FiAccount =
    FiAccount.findById(FiAccount.createWithAttributes(
       'accountId -> accountId, 'category -> category, 'currency -> currency,
       'fiCode -> s"$category-$currency", 'fiAccountId -> s"FI$accountId")).get
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
      create table cash_in_out (id bigint auto_increment, abs_amount decimal(20,4) not null, account_id varchar(32) not null, cashflow_id bigint, currency varchar(3) not null, event_day date not null, request_date timestamp not null, request_day date not null, self_fi_account_id varchar(32) not null, self_fi_code varchar(32) not null, status_type varchar(255) not null, target_fi_account_id varchar(32) not null, target_fi_code varchar(32) not null, value_day date not null, withdrawal boolean not null, primary key (id));
      create table fi_account (id bigint auto_increment, account_id varchar(32) not null, category varchar(30) not null, currency varchar(3) not null, fi_account_id varchar(32) not null, fi_code varchar(32) not null, primary key (id));
      create table holiday (id bigint auto_increment, category varchar(30) not null, day date not null, name varchar(40) not null, primary key (id));
      create table login (id varchar(32) not null, login_id varchar(255), password varchar(256) not null, primary key (id));
      create table self_fi_account (id bigint auto_increment, category varchar(30) not null, currency varchar(3) not null, fi_account_id varchar(32) not null, fi_code varchar(32) not null, primary key (id));
      create table staff (id varchar(32) not null, name varchar(30) not null, password varchar(256) not null, primary key (id));
      create table staff_authority (id bigint auto_increment, authority varchar(30) not null, staff_id varchar(32) not null, primary key (id));
    """).execute.apply()
  }
}
