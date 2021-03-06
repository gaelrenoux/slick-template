package slicktemplate

import com.typesafe.scalalogging.Logger
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slicktemplate.dao._
import slicktemplate.model.{Color, Lineage, Student}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object Main extends App {

  val log = Logger[Main.type]


  /* Injection is done manually through the constructor. Do whatever you want - use Guice, the Cake Pattern, MacWire,
  whatever */
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("hsqldb")

  val db = new AppDatabase(dbConfig)
  val houseDao = new HouseDao(dbConfig.profile)
  val studentDao = new StudentDao(houseDao, dbConfig.profile)
  val queries = new Queries(dbConfig.profile)

  import dbConfig.profile.api._

  val dbio: DBIOAction[Option[Student], NoStream, Effect.All] = for {
    /* You'll probably want to manage your DB structure some other way, but creating through Slick is possible */
    _ <- houseDao.createTable andThen studentDao.createTable
    _ = log.debug("Tables created")
    _ <- DBIO.successful(42)
    _ <- DBIO.from(Future(42))
    _ = log.debug("Because we can")
    gry <- houseDao.add(Houses.Gryffindor)
    huf <- houseDao.add(Houses.Hufflepuff)
    rav <- houseDao.add(Houses.Ravenclaw)
    sly <- houseDao.add(Houses.Slytherin)
    _ = log.debug("Houses created")
    _ <- studentDao.addAll(Students.AllGryffindor(gry.id))
    _ <- studentDao.addAll(Students.AllHufflepuff(huf.id))
    _ <- studentDao.addAll(Students.AllRavenclaw(rav.id))
    _ <- studentDao.addAll(Students.AllSlytherin(sly.id))
    _ = log.debug("Students created")
    allColors <- queries.getAllColors
    _ = log.debug(s"All colors: $allColors")
    pointsColors <- queries.getColors(minPoints = 1000, primary = true, secondary = false)
    _ = log.debug(s"Colors with points: $pointsColors")
    _ <- queries.forceColor(Color(0x000000))
    _ <- queries.resetPoints(0L)
    _ = log.debug("Points reset")

    houses <- houseDao.list()
    _ = log.debug(s"Houses: $houses")
    harry <- studentDao.find(Student.Filter(name = Some("Harry Potter")))
    _ = log.debug(s"Harry: $harry")
    countsP <- queries.countInHouses(Lineage.PureBlood)
    _ = log.debug(s"Pure Blooded: $countsP")
    countsNP <- queries.countInHouses(Seq(Lineage.MuggleBorn, Lineage.HalfBlood))
    _ = log.debug(s"Others: $countsNP")
    _ <- DBIO.successful(())

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
