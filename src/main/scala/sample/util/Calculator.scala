package sample.util

import java.util.concurrent.atomic.AtomicReference

import scala.math.BigDecimal.RoundingMode

/**
 * 計算ユーティリティ。
 */
case class Calculator(
  /** 現在の計算値 */
  value: AtomicReference[math.BigDecimal],
  /** 小数点以下桁数 */
  scale: Int,
  /** 端数定義。標準では切り捨て */
  mode: RoundingMode.Value,
  /** 計算の都度端数処理をする時はtrue */
  roundingAlways: Boolean) {
  /** scale未設定時の除算scale値 */
  private val defaultScale: Int = 18
  
  private def set(v: BigDecimal): Calculator = {
    value.set(rounding(v))
    this
  }

  def +(v: BigDecimal) =  set(decimal + v)
  def +(v: Number) = set(decimal + BigDecimal(v.toString()))

  private def rounding(v: BigDecimal): BigDecimal =
    if (roundingAlways) v.setScale(scale, mode) else v

  def -(v: BigDecimal) = set(decimal - v)
  def -(v: Number) = set(decimal - BigDecimal(v.toString()))

  def *(v: BigDecimal) = set(decimal * v)
  def *(v: Number) = set(decimal * BigDecimal(v.toString()))

  def /(v: BigDecimal) = set(decimal / v)
  def /(v: Number) = set(decimal / BigDecimal(v.toString()))

  /** 計算結果をint型で返します */
  def int:Int = decimal.intValue()

  /** 計算結果をlong型で返します。 */
  def long = decimal.longValue()

  /** 計算結果をBigDecimal型で返します。 */
  def decimal: BigDecimal =
    Option(value.get()).getOrElse(BigDecimal(0)).setScale(scale, mode)
}

object Calculator {
  def apply(): Calculator = apply(BigDecimal("0"))
  def apply(scale: Int, mode: RoundingMode.Value): Calculator = apply(BigDecimal("0"), scale, mode)
  def apply(scale: Int, mode: RoundingMode.Value, roundingAlways: Boolean): Calculator = apply(BigDecimal("0"), scale, mode, roundingAlways)
  def apply(v: String): Calculator = apply(BigDecimal(v))
  def apply(v: String, scale: Int, mode: RoundingMode.Value): Calculator = apply(BigDecimal(v), scale, mode)
  def apply(v: String, scale: Int, mode: RoundingMode.Value, roundingAlways: Boolean): Calculator = apply(BigDecimal(v), scale, mode, roundingAlways)
  def apply(v: Number): Calculator = apply(v.toString())
  def apply(v: Number, scale: Int, mode: RoundingMode.Value): Calculator = apply(v.toString(), scale, mode)
  def apply(v: Number, scale: Int, mode: RoundingMode.Value, roundingAlways: Boolean): Calculator = apply(v.toString(), scale, mode, roundingAlways)
  def apply(v: BigDecimal): Calculator =  apply(v, 0, RoundingMode.DOWN)
  def apply(v: BigDecimal, scale: Int, mode: RoundingMode.Value): Calculator = apply(v, scale, mode, false)
  def apply(v: BigDecimal, scale: Int, mode: RoundingMode.Value, roundingAlways: Boolean): Calculator =
    new Calculator(new AtomicReference(v), scale, mode, roundingAlways)
}
