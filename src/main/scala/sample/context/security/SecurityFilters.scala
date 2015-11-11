package sample.context.security

import javax.servlet.Filter

/**
 * Spring Securityに対するFilter拡張設定。
 * <p>Filterを追加したい時は本I/Fを継承してBean登録してください。
 */
trait SecurityFilters {
  
  /**
   * Spring SecurityへFilter登録するServletFilter一覧を返します。
   * <p>登録したFilterはUsernamePasswordAuthenticationFilter/ActorSessionFilterの後に
   * 実行されるのでActorSessionからログイン利用者の情報を取ることが可能です。
   */
  def filters(): Seq[Filter]
  
}