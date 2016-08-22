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

import com.websudos.phantom.dsl.{DateTime, _}
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._

/**
  * Some utilities to aid in test-data-generation
  */
trait GeneratorUtils {

  /**
    * Create a stream of generated elements
    */
  implicit def genToStream[T](genT: Gen[T]): Stream[T] = Stream.continually(genT.sample).flatten

  /**
    * Create a stream of arbitrary elements
    */
  def arbitraryStream[T: Arbitrary]: Stream[T] = arbitrary[T]

  /**
    * Generator for dates between the given bounds
    */
  def genDateBetween(lowerBound: DateTime, upperBound: DateTime): Gen[DateTime] =
    Gen.choose(lowerBound.getMillis, upperBound.getMillis).map(new DateTime(_))
}

object GeneratorUtils extends GeneratorUtils
