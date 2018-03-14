package psug20180322

import slick.jdbc.JdbcProfile

class Tables(profile: JdbcProfile) {

  import profile.api._

  val createHouse =
    sqlu"""drop table if exists house""" andThen
    sqlu"""
      create table house (
        id integer primary key,
        name varchar(255) not null,
        primary_color int not null,
        secondary_color int not null,
        points integer
      )
      """

  val createStudent =
    sqlu"""drop table if exists student""" andThen
    sqlu"""
      create table student (
        id integer primary key,
        name varchar(255) not null,
        house_id integer not null references house(id),
        lineage varchar(10) not null,
        updated timestamp not null
      )
      """
}
