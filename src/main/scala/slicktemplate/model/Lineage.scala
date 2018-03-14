package slicktemplate.model

sealed trait Lineage {
  override def toString: String = getClass.getSimpleName.dropRight(1)
}

object Lineage {
  object PureBlood extends Lineage
  object HalfBlood extends Lineage
  object MuggleBorn extends Lineage

  val LineageToString: Map[Lineage, String] = Map(
    Lineage.PureBlood -> "pure",
    Lineage.HalfBlood -> "half",
    Lineage.MuggleBorn -> "muggle"
  )
  val StringToLineage: Map[String, Lineage] = LineageToString.map(_.swap)
}
