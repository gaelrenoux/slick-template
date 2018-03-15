package slicktemplate.dao

import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, H2Profile}

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

  /** Health check on the database */
  def health: Future[Boolean] = dbConfig.db.run(Query(1).result.head.map(_ == 1))

}

object ComplicatedDatabase {
  implicit val profile: H2Profile.type = H2Profile
  implicit val api: H2Profile.API = profile.api
}