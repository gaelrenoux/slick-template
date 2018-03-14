package psug20180322.dao

import psug20180322.model.{Color, House}
import slick.jdbc.JdbcType
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

import scala.concurrent.ExecutionContext

class HouseDao(implicit ec: ExecutionContext) {

  // Bring the Slick DSL into scope
  import AppDatabase.api._


  private implicit val colorJdbcType: JdbcType[Color] =
    MappedColumnType.base[Color, Int](
      c => c.hexValue,
      i => Color(i)
    )

  private[dao] class HouseTable(tag: Tag) extends Table[House](tag, "house") {
    def id = column[Option[Long]]("id", O.AutoInc)

    def name = column[String]("name")

    def primaryColor = column[Color]("primary_color")

    def secondaryColor = column[Color]("secondary_color")

    def points = column[Long]("points")

    def pk = primaryKey("pk_house", id)

    def * = (id, name, primaryColor, secondaryColor, points) <>
      ((House.apply _).tupled, House.unapply)
  }

  private[dao] val table = TableQuery[HouseTable]

  def add(h: House): DBIOAction[Long, NoStream, Effect.Write] =
    (table returning table.map(_.id.get)) += h
  // table returning table += h
  // SQLite is not capable of returning the full record when doing an insert (SQLite's limit, not Slick's)

  def addAll(seq: Iterable[House]): DBIOAction[Seq[Long], NoStream, Effect.Write] =
    (table returning table.map(_.id.get)) ++= seq
  // table returning table ++= seq
  // SQLite is not capable of returning the full record when doing an insert (SQLite's limit, not Slick's)

  def get(hid: Long): DBIOAction[Option[House], NoStream, Effect.Read] =
    table.filter(_.id === hid).take(1).result.headOption

  def delete(id: Long): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === id).delete.map(_ > 0)

  def update(house: House): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === house.id).update(house).map(_ > 0)

  def getTop: FixedSqlStreamingAction[Seq[String], String, Effect.Read] =
    table.filter(_.points > 0L).sortBy(_.points.desc).map(_.name).result

  /** Returns true if it already existed */
  def upsert(house: House): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.insertOrUpdate(house).map(_ > 0)

  def list(filter: House.Filter = House.Filter.Empty): DBIOAction[Seq[House], Streaming[House], Effect.Read] =
    withFilter(filter).sortBy(_.id.desc).result

  def find(filter: House.Filter): DBIOAction[Option[House], NoStream, Effect.Read] =
    withFilter(filter).take(1).result.headOption

  def deleteAll(filter: House.Filter): DBIOAction[Int, NoStream, Effect.Write] =
    withFilter(filter).delete

  private def withFilter(filter: House.Filter) = {
    var query: Query[HouseTable, House, Seq] = table
    filter.name foreach { n => query = query.filter(_.name === n) }
    filter.anyColor foreach { color => query = query.filter(h => h.primaryColor === color || h.secondaryColor === color) }
    query
  }

}
