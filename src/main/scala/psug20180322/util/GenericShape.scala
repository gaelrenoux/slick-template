package psug20180322.util

import slick.ast.Node
import slick.lifted.{FlatShapeLevel, Rep, Shape}
import slick.model.Column

/**
  * Mixed is Seq[Rep[_]]
  * Unpacked is Map[String, Any]
  * Packed is Nothing
  */
class GenericShape extends Shape[FlatShapeLevel, Seq[Rep[_]], Map[String, Any], Nothing] {

  /** Mixed is Seq[Rep[_]] */
  override def pack(value: Mixed) = ???

  override def packedShape = ???

  override def buildParams(extract: Any => Unpacked) = ???

  override def encodeRef(value: Mixed, path: Node) = ???

  override def toNode(value: Mixed) = ???
}
