/*
 * Copyright 2015 Merlijn Boogerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mboogerd.lab.cassandra.part1

import com.github.mboogerd.lab.cassandra.part1.Part1Spec.Person
import com.github.mboogerd.lab.cassandra.testspec.CassandraTestSpec
import com.websudos.phantom.dsl.{UUID, _}

import scala.concurrent.Future
/**
  *
  */
object Part1Spec {
  case class Person(id: UUID, name: String, firstName: String)
}
trait Part1Spec extends CassandraTestSpec {

  override type DB = PeopleDatabase
  override lazy val database: PeopleDatabase = new PeopleDatabase()


  /* Boilerplate to define a table from uuid's to Person */
  class PeopleById extends CassandraTable[ConcretePeopleById, Person] {
    object id extends UUIDColumn(this) with PartitionKey[UUID]
    object name extends StringColumn(this)
    object firstName extends StringColumn(this)

    def fromRow(row: Row): Person = Person(id(row), name(row), firstName(row))
  }

  abstract class ConcretePeopleById extends PeopleById with RootConnector {
    def byId(id: UUID): Future[Option[Person]] = select.where(_.id eqs id).one()
    def byIds(ids: List[UUID]): Future[Seq[Person]] = select.where(_.id in ids).fetch()
  }


  /* Boilerplate to define a table from firstName to (Person) uuid's */
  class PeopleIdsByFirstName extends CassandraTable[ConcretePeopleIdsByFirstName, (String, UUID)] {
    object firstName extends StringColumn(this) with PartitionKey[String]
    object id extends UUIDColumn(this) with PrimaryKey[UUID]

    def fromRow(row: Row): (String, UUID) = (firstName(row), id(row))
  }

  abstract class ConcretePeopleIdsByFirstName extends PeopleIdsByFirstName with RootConnector {
    def idsFromFirstName(firstName: String): Future[List[UUID]] =
      select(_.id).where(_.firstName eqs firstName).fetch()
  }

  /* Abstraction for all People-database operations */
  trait PeopleDao {
    def byId(id: UUID): Future[Option[Person]]
    def byFirstName(firstName: String): Future[Seq[Person]]
    def insertPerson(person: Person): Future[ResultSet]
  }

  class PeopleDatabase extends Database(embeddedConnector) with PeopleDao {
    object peopleId extends ConcretePeopleById with embeddedConnector.Connector
    object peopleFirstName extends ConcretePeopleIdsByFirstName with embeddedConnector.Connector

    override def byId(id: UUID): Future[Option[Person]] = peopleId.byId(id)

    override def byFirstName(firstName: String): Future[Seq[Person]] = for {
      ids ← peopleFirstName.idsFromFirstName(firstName)
      persons ← peopleId.byIds(ids)
    } yield persons

    override def insertPerson(person: Person): Future[ResultSet] = {
      peopleId.insert.value(_.id, person.id)
        .value(_.name, person.name)
        .value(_.firstName, person.firstName)
        .future().flatMap {
        _ ⇒ {
          peopleFirstName.insert
            .value(_.firstName, person.firstName)
            .value(_.id, person.id)
            .future()
        }
      }
    }
  }
}
