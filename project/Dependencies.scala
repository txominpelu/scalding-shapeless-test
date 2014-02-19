/*
 * Copyright (c) 2012 SnowPlow Analytics Ltd. All rights reserved.
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
import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    "Concurrent Maven Repo" at "http://conjars.org/repo" // For Scalding, Cascading etc
  )

  object V {
    val scalding  = "0.9.0rc4"
    val hadoop    = "2.0.0-mr1-cdh4.1.2"
    val specs2    = "1.13" // -> "1.13" when we bump to Scala 2.10.0
    val cascadingVersion = "2.0.7"
    // Add versions for your additional libraries here...
  }

  object Libraries {
    val scaldingCore = "com.twitter"                %%  "scalding-core"       % V.scalding
    val scaldingAvro = "com.twitter"                %%  "scalding-avro"       % V.scalding
    val hadoopCore   = "org.apache.hadoop"          % "hadoop-core"           % V.hadoop       % "provided"
    // Add additional libraries from mvnrepository.com (SBT syntax) here...

    // Scala (test only)
    val specs2       = "org.specs2"                 %% "specs2"               % V.specs2       % "test"
    val shapeless    = "com.chuusai" % "shapeless" % "2.0.0-M1" cross CrossVersion.full exclude("log4j","log4j")
    val log4j        = "log4j" % "log4j" % "1.2.17"

  }
}
