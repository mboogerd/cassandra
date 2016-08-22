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

package com.github.mboogerd.lab.cassandra.testspec

import com.github.mboogerd.lab.cassandra.DatabaseProvider
import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Inspired by "Testing with phantom-sbt", which can be found here:
  * http://outworkers.com/blog/post/phantom-tips-tip2-testing-with-phantom-sbt
  */
trait CassandraTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with DatabaseProvider with GeneratorUtils {

  behavior of "Cassandra"

  val keySpaceName = "phantomTest"

  lazy val embeddedConnector = ContactPoint.embedded.keySpace(keySpaceName)
  private implicit lazy val session: Session = embeddedConnector.session
  private implicit lazy val keyspace: KeySpace = database.space

  override protected def beforeAll(): Unit = {
    database.initialize()
    // auto-create the database schema, and await its completion before running the test
    Await.result(database.autocreate().future(), 10.seconds)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    // Ensure proper suite isolation by cleaning the schema after all tests
    Await.result(database.autodrop().future(), 10.seconds)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    // Ensure proper test-isolation by cleaning tables between each test
    Await.result(database.autotruncate.future(), 5.seconds)
  }
}
