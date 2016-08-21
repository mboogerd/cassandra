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

import java.util.UUID

import com.github.mboogerd.lab.cassandra.part1.Part1Spec.Person
import com.websudos.phantom.dsl._


class Part1Test extends Part1Spec {

  val me: Person = Person(UUID.randomUUID(), "Boogerd", "Merlijn")

  it should "return no person by id if the database is empty" in {
    database.byId(me.id).futureValue shouldBe empty
  }

  it should "return no person by firstname if the database is empty" in {
    database.byFirstName(me.firstName).futureValue shouldBe empty
  }

  it should "retrieve persons by id if successfully persisted" in {
    val retrieval = for {
      persisted ← database.insertPerson(me)
      retrieved ← database.byId(me.id)
    } yield retrieved

    retrieval.futureValue shouldBe Some(me)
  }


  it should "retrieve persons by firstname if successfully persisted" in {
    val retrieval = for {
      persisted ← database.insertPerson(me)
      retrieved ← database.byFirstName(me.firstName)
    } yield retrieved

    retrieval.futureValue should contain only me
  }
}
