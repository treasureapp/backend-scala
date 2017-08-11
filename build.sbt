name := "treasureapp"

version := "1.0"

scalaVersion := "2.11.8"


/**
  * logging
  */
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

/**
  * xml
  */
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"

/**
  * akka
  */
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.6",
  "com.typesafe.akka" %% "akka-actor" % "2.5.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.2"
)

/**
  * ScalaTest
  */
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

/**
  * Spark
  */
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.1",
  "org.apache.spark" %% "spark-sql" % "2.1.1",
  "org.apache.spark" %% "spark-streaming" % "2.1.1",
  "org.apache.spark" %% "spark-hive" % "2.1.1",
  "org.apache.spark" %% "spark-mllib" % "2.1.1"
)

/**
  * Date library
  * https://github.com/nscala-time/nscala-time
  */
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"

/**
  * spire
  * https://github.com/non/spire
  */
libraryDependencies += "org.typelevel" %% "spire" % "0.14.1"
libraryDependencies += "org.typelevel" %% "spire-extras" % "0.14.1"

/**
  * toCSV
  * https://github.com/melrief/PureCSV
  */
//resolvers += Resolver.sonatypeRepo("releases")
//libraryDependencies += "com.github.melrief" %% "purecsv" % "0.0.9"


/**
  * http://www.scala-sbt.org/0.13/docs/Howto-Scaladoc.html
  */
scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits")
