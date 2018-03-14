package psug20180322.dao

import java.time._

import psug20180322.model.{House, Lineage, Student}
import psug20180322.util.SlickJdbcTypes
import slick.jdbc.JdbcType
import slick.sql.SqlAction

import scala.concurrent.ExecutionContext

class StudentDao(val houseDao: HouseDao)(implicit ec: ExecutionContext) {

  // Bring the Slick DSL into scope
  import AppDatabase.api._


  private val moreJdbcTypes = new SlickJdbcTypes

  import moreJdbcTypes.instantWithoutTimezoneType

  private implicit val lineageType: JdbcType[Lineage] = moreJdbcTypes.projected(
    Lineage.PureBlood -> "pure",
    Lineage.HalfBlood -> "half",
    Lineage.MuggleBorn -> "muggle"
  )

  private[dao] class StudentTable(tag: Tag) extends Table[Student](tag, "student") {
    def id = column[Option[Long]]("id", O.AutoInc)

    def name = column[String]("name")

    def houseId = column[Long]("house_id")

    def lineage = column[Lineage]("lineage")

    def updated = column[Instant]("updated")

    def pk = primaryKey("pk_student", id)

    def fkHouseId = foreignKey("fk_student_house_id", houseId, houseDao.table)(_.id.get, onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, houseId, lineage, updated) <>
      ((Student.apply _).tupled, Student.unapply)
  }


  private[dao] val table = TableQuery[StudentTable]


  def add(s: Student): DBIOAction[Long, NoStream, Effect.Write] =
    (table returning table.map(_.id.get)) += s.copy(updated = Instant.now())
  // (table returning table) += s.copy(updated = Instant.now())
  // SQLite is not capable of returning the full record when doing an insert (SQLite's limit, not Slick's)

  def addAll(seq: Iterable[Student]): DBIOAction[Seq[Long], NoStream, Effect.Write] =
    (table returning table.map(_.id.get)) ++= seq.map(_.copy(updated = Instant.now()))
  // (table returning table) ++= seq.map(_.copy(updated = Instant.now()))
  // SQLite is not capable of returning the full record when doing an insert (SQLite's limit, not Slick's)

  def get(id: Long): DBIOAction[Option[Student], NoStream, Effect.Read] =
    table.filter(_.id === id).take(1).result.headOption

  def delete(id: Long): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === id).delete.map(_ > 0)

  def update(student: Student): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === student.id).update(student).map(_ > 0)

  def upsert(student: Student): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.insertOrUpdate(student.copy(updated = Instant.now())).map(_ > 0)

  def getWithHouse(id: Long): SqlAction[Option[(Student, House)], NoStream, Effect.Read] = {
    val query = for {
      student <- table.filter(_.id === id)
      house <- student.fkHouseId
    } yield (student, house)
    query.take(1).result.headOption
  }

  def countByHouse =
    table.groupBy(_.houseId).result

  def list(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Seq[Student], Streaming[Student], Effect.Read] =
    withFilter(filter).sortBy(_.id.desc).result

  def find(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Option[Student], NoStream, Effect.Read] =
    withFilter(filter).take(1).result.headOption

  def deleteAll(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Int, NoStream, Effect.Write] =
    withFilter(filter).delete

  private def withFilter(filter: Student.Filter) = {
    var query: Query[StudentTable, Student, Seq] = table
    filter.name foreach { n => query = query.filter(_.name === n) }
    filter.houseId foreach { hid => query = query.filter(_.houseId === hid) }
    filter.lineage foreach { lin => query = query.filter(_.lineage === lin) }
    query
  }

}
