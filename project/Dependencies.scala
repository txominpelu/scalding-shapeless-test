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
    "Concurrent Maven Repo" at "http://conjars.org/repo", // For Scalding, Cascading etc
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Cloudera Repository" at "https://repository.cloudera.com/artifactory/public/",
    "Twitter Maven Repo" at "http://maven.twttr.com",
    "Maven Repository" at "http://mvnrepository.com/artifact/",
    "Apache public" at "https://repository.apache.org/content/groups/public",
    "Cloudera2" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  object V {
    val scalding  = "0.8.11"
    val hadoop    = "2.0.0-mr1-cdh4.4.0"
    val specs2    = "1.13" // -> "1.13" when we bump to Scala 2.10.0
    val cascadingVersion = "2.2.0"
    // Add versions for your additional libraries here...
  }

  object Libraries {
    val hadoopVersion = "2.0.0-cdh4.1.2"
    val hadoopMr1Version = "2.0.0-mr1-cdh4.1.2"
    val scaldingVersion = "0.9.0rc4"

    val cascadingCore   = "cascading"          % "cascading-core"           % V.cascadingVersion
    val scaldingArgs            = "com.twitter"                       %% "scalding-args"             % scaldingVersion
    val scaldingAvro            = "com.twitter"                       %% "scalding-avro"             % scaldingVersion
    val scaldingCommons         = "com.twitter"                       %% "scalding-commons"          % "0.2.0"            exclude ("jvyaml", "jvyaml")
    val scaldingCore            = "com.twitter"                       %% "scalding-core"             % scaldingVersion
    val hadoopCommon            = "org.apache.hadoop"                 %  "hadoop-common"             % hadoopVersion      exclude ("org.slf4j", "slf4j-log4j12")
     val hadoopHdfs              = "org.apache.hadoop"                 %  "hadoop-hdfs"               % hadoopVersion      exclude ("org.slf4j", "slf4j-log4j12") exclude ("commons-daemon", "commons-daemon")
     val hadoopCore              = "org.apache.hadoop"                 %  "hadoop-core"               % hadoopMr1Version
    // Add additional libraries from mvnrepository.com (SBT syntax) here...

    // Scala (test only)
    val specs2       = "org.specs2"                 %% "specs2"               % V.specs2       % "test"
    val shapeless    = "com.chuusai" % "shapeless" % "2.0.0-SNAPSHOT" cross CrossVersion.full changing()
    val log4j        = "log4j" % "log4j" % "1.2.17"

  }
}
