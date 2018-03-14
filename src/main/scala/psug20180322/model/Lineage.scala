package psug20180322.model

sealed trait Lineage {
  override def toString: String = getClass.getSimpleName
}

object Lineage {
  object PureBlood extends Lineage
  object HalfBlood extends Lineage
  object MuggleBorn extends Lineage
}
