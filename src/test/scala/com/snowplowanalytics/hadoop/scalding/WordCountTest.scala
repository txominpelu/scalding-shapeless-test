/*
 * Copyright (c) 2012 Twitter, Inc.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.hadoop.scalding

// Specs2
import org.specs2.mutable.Specification
import shapeless.ops.record.{Remover, Updater}

// Scalding
import com.twitter.scalding._

import org.specs2.mutable.Specification

import TDsl._

import shapeless._
import record._
import syntax.singleton._

object TUtil {
  def printStack( fn: => Unit ) {
    try { fn } catch { case e : Throwable => e.printStackTrace; throw e }
  }
}

object Utils {
  val (wCount, wWord) = (Witness("count"), Witness("word"))
  type WordCount = :: [shapeless.record.FieldType[wWord.T,String], ::[shapeless.record.FieldType[wCount.T,Long], HNil]] with Serializable
}

class TypedPipeTest extends Specification {
  import Dsl._
  "A TypedPipe" should {
    TUtil.printStack {
      JobTest(new TypedPipeJob(_)).
        source(TextLine("inputFile"), List("0" -> "hack hack hack and hack")).
        sink[(String,Long)](TypedTsv[(String,Long)]("outputFile")){ outputBuffer =>
        val outMap = outputBuffer.toMap
        "count words correctly" in {
          outMap("hack") must be_==(4)
          outMap("and") must be_==(1)
        }
      }.
      run.
      runHadoop.
      finish
    }
  }
}

class TypedPipeJob(args : Args) extends Job(args) {

  //Word count using TypedPipe
  TextLine("inputFile")
    .flatMap { _.split("\\s+") }
    .map { w => "word" ->> w :: "count" ->> 1L :: HNil }
    .map { w =>
      val toAdd = ("tal" ->> "cual")
      val wordTal = Witness("word")
      w.remove("word")
      val tTal = implicitly[Remover[Utils.WordCount, wordTal.T]]
      println(tTal)
      val witnessTal = Witness("tal")
      val t = implicitly[Updater[Utils.WordCount, shapeless.record.FieldType[witnessTal.T,String]]]
      t
      w + toAdd
    }
    //.forceToDisk
    // I would like to write groupByField("word") and fail if there's not such field (and it would drop the field from the record)
    .groupBy((b) => b("word") )
    // word passes as an aggregation key but is dropped from the value record
    .mapValues((x) => x.drop(Nat._1))
    .mapValues(_("count"))
    // I would like to write sum and have the monoid generated for me
    // or I would like to say monoidWithFields ("count") and it would generate a monoid that adds the count field and ignores the others
    .sum// (sMonoid)
    //.map(x => (x._1, x._2("count")))
    //.forceToReducers
    //.debug
    .write(TypedTsv[(String, Long)]("outputFile"))
}