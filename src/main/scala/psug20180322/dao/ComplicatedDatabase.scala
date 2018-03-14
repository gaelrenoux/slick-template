package psug20180322.dao

import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, SQLiteProfile}

import scala.concurrent.{ExecutionContext, Future}

class ComplicatedDatabase(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  private lazy val api = dbConfig.profile.api

  import api._

  /** Runs a Slick action on the master database */
  def onMaster[T](action: DBIOAction[T, _ <: NoStream, Effect.All]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Runs a Slick action on a slave database, read-only !*/
  def onSlave[T](action: DBIOAction[T, _ <: NoStream, Effect.Read]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Health check on the application database */
  def health: Future[Boolean] = dbConfig.db.run(sql"""select 1""".as[Int].head map (_ == 1))

}

object ComplicatedDatabase {
  implicit val profile: SQLiteProfile.type = SQLiteProfile
  implicit val api: SQLiteProfile.API = profile.api
}