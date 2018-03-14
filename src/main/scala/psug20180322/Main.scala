package psug20180322

import com.typesafe.scalalogging.Logger
import psug20180322.dao.AppDatabase.api
import psug20180322.dao._
import psug20180322.model.Student
import slick.basic.DatabaseConfig
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object Main extends App {

  val log = Logger[Main.type]

  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("litedb")

  val db = new AppDatabase(dbConfig)
  val houseDao = new HouseDao
  val studentDao = new StudentDao(houseDao)
  val queries = new Queries
  val tables = new Tables(AppDatabase.profile)


  val dbio: DBIOAction[Option[Student], NoStream, Effect.All] = for {
    _ <- tables.createHouse andThen tables.createStudent
    _ = log.debug("Tables created")
    gryId <- houseDao.add(Houses.Gryffindor)
    hufId <- houseDao.add(Houses.Hufflepuff)
    ravId <- houseDao.add(Houses.Ravenclaw)
    slyId <- houseDao.add(Houses.Slytherin)
    _ = log.debug("Houses created")
    _ <- studentDao.addAll(Students.AllGryffindor(gryId))
    _ <- studentDao.addAll(Students.AllHufflepuff(hufId))
    _ <- studentDao.addAll(Students.AllRavenclaw(ravId))
    _ <- studentDao.addAll(Students.AllSlytherin(slyId))
    _ = log.debug("Students created")
    _ <- queries.resetPoints
    _ = log.debug("Points reset")

    houses <- houseDao.list()
    _ = log.debug(s"Houses: $houses")
    harry <- studentDao.find(Student.Filter(name = Some("Harry Potter")))
    _ = log.debug(s"Harry: $harry")
    counts <- queries.countInHouses
    _ = log.debug(s"Counts: $counts")

  } yield harry

  val f: Future[Option[Student]] = db(dbio)

  /* A small demo of using the Effect phantom type to control access */
  val complicatedDb = new ComplicatedDatabase(dbConfig)

  val readOnMaster = complicatedDb.onMaster(studentDao.get(1))
  val readOnSlave = complicatedDb.onSlave(studentDao.get(1))
  val writeOnMaster = complicatedDb.onMaster(studentDao.deleteAll())
  //val writeOnSlave = masterSlaveDb.onSlave(studentDao.deleteAll()) //doesn't compile !

  /* It also works for combined actions */
  val readRead: DBIOAction[Option[Student], NoStream, Effect.Read] = studentDao.get(1) andThen studentDao.get(1)
  complicatedDb.onMaster(readRead)
  complicatedDb.onSlave(readRead)
  val readWrite: DBIOAction[Boolean, NoStream, Effect.Read with Effect.Write] = studentDao.get(1) andThen studentDao.delete(1)
  complicatedDb.onMaster(readWrite)
  //complicatedDb.onSlave(readWrite) //doesn't compile !

  Await.result(f, 1 minute)
}
