package slicktemplate.dao

import slicktemplate.model.{Color, Lineage}

class Queries {

  // Bring the Slick DSL into scope
  import AppDatabase.api._
  import slicktemplate.util.GetResults._
  import slicktemplate.util.SQLActionBuilderOps._
  import slicktemplate.util.SetParameters._

  SetInstant //prevent IntelliJ from removing the previous import on cleanup, it doesn't see it's necessary

  val countInHouses: DBIOAction[Vector[(String, Long)], Streaming[(String, Long)], Effect.Read] =
    sql"""
        select h.name, count(*)
        from student s
        join house h on h.id = s.house_id
        group by h.name
      """.as[(String, Long)]

  def resetPoints(value: Long): DBIOAction[Int, NoStream, Effect.Write] =
    sqlu" update house set points = $value "

  def getAllColors: DBIOAction[Seq[Color], Streaming[Color], Effect.Read] =
    sql"""
        select distinct color from (
          select primary_color color from house
          union
          select secondary_color color from house
        )
      """.as[Color]

  def forceColor(color: Color): DBIOAction[Int, NoStream, Effect.Write] =
    sqlu" update house set primary_color = $color"

  def countInHouses(lineage: Lineage): DBIOAction[Seq[(String, Long)], Streaming[(String, Long)], Effect.Read] =
    sql"""
        select h.name, count(*)
        from student s
        join house h on h.id = s.house_id
        where s.lineage=$lineage
        group by h.name
      """.as[(String, Long)]

  def getColors(minPoints: Long, primary: Boolean = true, secondary: Boolean = true): DBIOAction[Seq[Color], Streaming[Color], Effect.Read] = {
    /* Needs SQLActionBuilderOps to bring + and mkSql */
    val getPrimary = if (primary) Some(sql" select distinct primary_color color from house where points > $minPoints ") else None
    val getSecondary = if (secondary) Some(sql" select distinct secondary_color color from house where points > $minPoints ") else None
    val union = Seq(getPrimary, getSecondary).flatten.mkSql("(", ") union (", ")")
    (sql"select distinct color from (" + union + sql")").as[Color]
  }


  def countInHouses(lineage: Seq[Lineage]): DBIOAction[Seq[(String, Long)], Streaming[(String, Long)], Effect.Read] = {
    /* Needs SQLActionBuilderOps to bring + and mkSql */
    val inQuery = lineage.map(l => sql"$l").mkSql(",")
    (
      sql"""
          select h.name, count(*) from student s
          join house h on h.id = s.house_id
          where s.lineage in ("""
      + inQuery
      +
      sql"""
          )
          group by h.name
        """
      ).as[(String, Long)]
  }

  def somethingComplicated: DBIOAction[Option[Int], NoStream, Effect.All] =
    SimpleDBIO { ctx =>
      /* Do whatever you want with that context, using JDBC ! */
      ctx.session.withPreparedStatement("some sql !") { stmt =>
        stmt.setString(1, "value")
        val rs = stmt.executeQuery()
        if (rs.next()) Some(rs.getInt(1))
        else None
      } // stmt is closed when closing the bracket
    }

}
