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

package com.github.mboogerd.lab.cassandra.gettingstarted

import java.util.UUID

import com.datastax.driver.core.{ConsistencyLevel, ResultSet, Row}
import com.github.mboogerd.lab.cassandra.gettingstarted.GettingStartedSpec.User
import com.github.mboogerd.lab.cassandra.testspec.CassandraTestSpec
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.dsl._
import com.websudos.phantom.keys.PartitionKey
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  *
  */
object GettingStartedSpec {

  /* User Entity */
  case class User(id: UUID, email: String, name: String, registrationDate: DateTime)
}

trait GettingStartedSpec extends CassandraTestSpec {

  override type DB = TestDatabase.type
  override lazy val database: TestDatabase.type = TestDatabase

  /* Users table */
  class Users extends CassandraTable[ConcreteUsers, User] {

    object id extends UUIDColumn(this) with PartitionKey[UUID]
    object email extends StringColumn(this)
    object name extends StringColumn(this)
    object registrationDate extends DateTimeColumn(this)

    def fromRow(row: Row): User = {
      User(
        id(row),
        email(row),
        name(row),
        registrationDate(row)
      )
    }
  }

  // The root connector comes from import com.websudos.phantom.dsl._
  abstract class ConcreteUsers extends Users with RootConnector {

    def store(user: User): Future[ResultSet] = {
      insert.value(_.id, user.id).value(_.email, user.email)
        .value(_.name, user.name)
        .value(_.registrationDate, user.registrationDate)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }

    def getById(id: UUID): Future[Option[User]] = {
      select.where(_.id eqs id).one()
    }
  }


  object TestDatabase extends Database(embeddedConnector) {

    object users extends ConcreteUsers with embeddedConnector.Connector

  }
}
