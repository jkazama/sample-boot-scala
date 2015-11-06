package sample.context.report

import sample.context.Dto

/** ファイルイメージを表現します。 */
case class ReportFile(
  name: String,
  data: Array[Byte]) extends Dto {
  def size = data.length
}
