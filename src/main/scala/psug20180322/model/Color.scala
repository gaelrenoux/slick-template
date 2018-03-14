package psug20180322.model

case class Color(hexValue: Int) extends AnyVal

object Color {

  val Scarlet = Color(0xff2400)
  val Gold = Color(0xffd700)
  val Yellow = Color(0xffff00)
  val Black = Color(0x000000)
  val Blue = Color(0x000080)
  val Bronze = Color(0xcd7f32)
  val Green = Color(0x7cfc00)
  val Silver = Color(0xc0c0c0)

}