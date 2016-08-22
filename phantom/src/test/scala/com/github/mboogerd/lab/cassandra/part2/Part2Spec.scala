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

package com.github.mboogerd.lab.cassandra.part2

import java.util.UUID

import com.datastax.driver.core.ConsistencyLevel
import com.github.mboogerd.lab.cassandra.part2.Part2Spec.{Person, Stock}
import com.github.mboogerd.lab.cassandra.testspec.CassandraTestSpec
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import org.scalacheck.Gen
import org.scalacheck.Arbitrary._
import org.scalacheck.Arbitrary
import org.scalatest.enablers.Aggregating
import play.api.libs.iteratee.Enumerator

import scala.collection.GenTraversable
import scala.concurrent.Future
import com.github.mboogerd.lab.cassandra.testspec.GeneratorUtils._
/**
  *
  */
object Part2Spec {
  case class Person(
                     id: UUID,
                     country: String,
                     name: String,
                     firstName: String,
                     phone: String,
                     eyeColor: String
                   )

  case class Stock(
                    id: UUID,
                    market: String,
                    symbol: String,
                    value: BigDecimal,
                    time: DateTime
                  )

  val countries = Seq("Netherlands", "Germany", "United Kingdom", "Belgium", "France")

  val genPerson: Gen[Person] = for {
    country ← Gen.oneOf(countries)
    name ← Gen.alphaStr
    firstName ← Gen.alphaStr
    phone ← Gen.identifier
    eyeColor ← Gen.identifier
  } yield Person(UUID.randomUUID(), country, name, firstName, phone, eyeColor)

  implicit val arbPerson = Arbitrary(genPerson)

  def genStock(lowerBound: DateTime, upperBound: DateTime): Gen[Stock] = for {
    market ← Gen.identifier
    symbol ← Gen.identifier
    value ← arbitrary[Long]
    time ← genDateBetween(lowerBound, upperBound)
  } yield Stock(UUID.randomUUID(), market, symbol, value, time)
}
trait Part2Spec extends CassandraTestSpec {

  override type DB = PeopleDatabase
  override lazy val database: PeopleDatabase = new PeopleDatabase()


  /* (Concrete)People table definition */

  class PeopleByCountry extends CassandraTable[ConcretePeopleByCountry, Person] {
    object country extends StringColumn(this) with PartitionKey[String]
    object id extends UUIDColumn(this) with PrimaryKey[UUID]
    object name extends StringColumn(this)
    object firstName extends StringColumn(this)
    object phone extends StringColumn(this)
    object eyeColor extends StringColumn(this)

    def fromRow(row: Row): Person = {
      Person(
        id(row),
        country(row),
        name(row),
        firstName(row),
        phone(row),
        eyeColor(row)
      )
    }
  }

  abstract class ConcretePeopleByCountry extends PeopleByCountry with RootConnector with PeopleDao {

    def byCountry(country: String): Enumerator[Person] = {
      select.where(_.country eqs country).fetchEnumerator()
      // does not compile!
      //select.where(_.country eqs country).limit(5000).setFetchSize(1000).fetchEnumerator
    }

    def store(person: Person): Future[ResultSet] = {
      insert
        .value(_.id, person.id)
        .value(_.country, person.country)
        .value(_.name, person.name)
        .value(_.firstName, person.firstName)
        .value(_.phone, person.phone)
        .value(_.eyeColor, person.eyeColor)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
  }


  /* (Concrete)Stocks table definition */

  class Stocks extends CassandraTable[ConcreteStocks, Stock] {
    object id extends UUIDColumn(this) with PartitionKey[UUID]
    object market extends StringColumn(this)
    object symbol extends StringColumn(this)
    object value extends BigDecimalColumn(this)
    object time extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

    def fromRow(row: Row): Stock = {
      Stock(
        id(row),
        market(row),
        symbol(row),
        value(row),
        time(row)
      )
    }
  }

  abstract class ConcreteStocks extends Stocks with RootConnector {

    def getEntriesForToday: Future[Seq[Stock]] = {
      // Get the start of the day using JodaTime
      val start = new DateTime().withTimeAtStartOfDay()

      // Use the default constructor to get "now".
      val end = new DateTime()

      // Do a range query, effectively saying: "Give me all records where the time is greater than start and lower than end".
      select.where(_.time gte start).and(_.time lte end).fetch()
    }
    
    def store(stock: Stock): Future[ResultSet] = {
      insert
        .value(_.id, stock.id)
        .value(_.market, stock.market)
        .value(_.symbol, stock.symbol)
        .value(_.value, stock.value)
        .value(_.time, stock.time)
        .consistencyLevel_=(ConsistencyLevel.ALL)
        .future()
    }
  }


  /* Abstraction for all People-database operations */
  trait PeopleDao {
    def byCountry(country: String): Enumerator[Person]
  }

  class PeopleDatabase extends Database(embeddedConnector) {
    object people extends ConcretePeopleByCountry with embeddedConnector.Connector
    object stocks extends ConcreteStocks with embeddedConnector.Connector
  }
}
