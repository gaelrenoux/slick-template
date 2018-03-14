package slicktemplate.util

import slick.jdbc.{PositionedParameters, SQLActionBuilder, SetParameter}

object SQLActionBuilderOps {

  /** Merge multiple SetParameter[Unit] functions, by calling them one after the other. */
  private class MergedSetParameters(funs: SetParameter[Unit]*) extends SetParameter[Unit] {
    override def apply(ignoredValue: Unit, params: PositionedParameters): Unit = funs foreach { f => f((), params) }
  }

  /**
    * Additional operations on the SQLActionBuilder, which is the object made by using the sql String prefix.
    */
  implicit class RichSQLActionBuilder(sql: SQLActionBuilder) {

    /** Concatenation operator between Slick SQL queries */
    def +(sql2: SQLActionBuilder): SQLActionBuilder = concat(sql2)

    /** Concatenation between Slick SQL queries */
    def concat(sql2: SQLActionBuilder): SQLActionBuilder = {
      val parts1 = sql.queryParts
      val parts2 = sql2.queryParts
      /* The intervals between parts are irrelevant in this case, so no need to merge the last element of the first
       sequence with the first element of the second sequence. We just concatenate the sequences. */
      val mergedParts = parts1 ++ parts2

      /** These elements store functions applying the needed parameters (parameters are included in the function,
        * hence the Unit type in SetParameter). */
      val fun1: SetParameter[Unit] = sql.unitPConv
      val fun2: SetParameter[Unit] = sql2.unitPConv
      val mergedFuns = new MergedSetParameters(fun1, fun2)

      SQLActionBuilder(mergedParts, mergedFuns)
    }
  }

  implicit class RichSeqSQLActionBuilder(seq: Seq[SQLActionBuilder]) {
    /** Just like mkString but for SQLActionBuilders */
    def mkSql(start: String, sep: String, end: String): SQLActionBuilder = {
      val finalIndex = seq.length - 1
      val parts = seq.map(_.queryParts).zipWithIndex.flatMap {
        case (qp, ix) if ix == 0 && ix == finalIndex => start +: qp :+ end //just one element !
        case (qp, ix) if ix == 0 => start +: qp :+ sep
        case (qp, ix) if ix < finalIndex => qp :+ sep
        case (qp, _) => qp :+ end
      }

      val setParams = new MergedSetParameters(seq.map(_.unitPConv): _*)

      SQLActionBuilder(parts, setParams)
    }

    def mkSql(sep: String): SQLActionBuilder = mkSql("", sep, "")
  }


}
