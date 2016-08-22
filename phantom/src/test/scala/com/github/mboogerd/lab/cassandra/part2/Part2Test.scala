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

import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._

import scala.concurrent.Future
import scala.util.Try


class Part2Test extends Part2Spec {

  import Part2Spec._

  it should "enumeration of a elements in a table" in {
    val people = arbitraryStream[Person].take(100)
    val countryPeople = people.groupBy(_.country)

    val writingPeople = Future.sequence(people.map(database.people.store))

    Try(writingPeople.futureValue) should be a 'success

    val peopleEnumerator = database.people.byCountry(countries.head)
    val retrievedPeople = peopleEnumerator.run(Iteratee.collect()).futureValue

    retrievedPeople should contain theSameElementsAs countryPeople(countries.head)
  }


  /*
  Note that this is an experiment, I wouldn't advise writing your test like this, as there are is potential both for
  false-positives and true-negatives.
  */
  ignore should "support time-series queries" in {
    val lastNight = new DateTime().withTimeAtStartOfDay()
    val yesterday = lastNight.minusDays(1)
    val tomorrow = lastNight.plusDays(1)

    val generatedStock: Stream[Stock] = genStock(yesterday, tomorrow).take(100)
    val writingStock = Future.sequence(generatedStock.map(database.stocks.store))

    Try(writingStock.futureValue) should be a 'success

    // FAILS due to: "Cannot execute this query as it might involve data filtering and thus may have unpredictable
    // performance. If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING."
    // FIXME: Previously, I believed ALLOW FILTERING to be an indicator for bad design. Need to research a bit more.
    val retrievedStock = database.stocks.getEntriesForToday.futureValue
    val expectedStock = generatedStock.filter(stock ⇒ stock.time.isAfter(lastNight) && stock.time.isBefore(new DateTime))

    retrievedStock should contain theSameElementsAs expectedStock
  }
}
