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
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.mboogerd.lab.cassandra.gettingstarted.GettingStartedSpec.User
import org.joda.time.DateTime


class GettingStarted extends GettingStartedSpec {

  import TestDatabase.users._

  it should "persist a new user" in {
    val user: User = User(UUID.randomUUID(), "test@example.com", "getting-started", DateTime.now())
    val persisting = store(user)
    persisting.futureValue.all() shouldBe empty
  }

  it should "retrieve a persisted user" in {
    val userToPersist: User = User(UUID.randomUUID(), "test@example.com", "getting-started", DateTime.now())

    val userRetrieval = for {
      persisted ← store(userToPersist)
      retrieved ← getById(userToPersist.id)
    } yield retrieved

    userRetrieval.futureValue shouldBe defined
    userRetrieval.futureValue.get shouldBe userToPersist
  }
}
