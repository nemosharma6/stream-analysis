name := "stream-analysis"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-core" % "4.0.7",
  "org.twitter4j" % "twitter4j-stream" % "4.0.7",
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-stream" % "2.5.21",
  "org.apache.kafka" %% "kafka" % "2.1.0",
  "org.apache.spark" %% "spark-core" % "2.3.0",
  "org.apache.spark" %% "spark-sql" % "2.3.0",
  "org.apache.spark" %% "spark-streaming" % "2.3.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.3.0" excludeAll(
    ExclusionRule(organization = "org.spark-project.spark", name = "unused"),
    ExclusionRule(organization = "org.apache.spark", name = "spark-streaming"),
    ExclusionRule(organization = "org.apache.hadoop")
  ),
  "com.aylien.textapi" % "client" % "0.6.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "org.elasticsearch" % "elasticsearch-hadoop" % "6.6.0",
  "com.sun.xml.bind" % "jaxb-impl" % "2.3.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.7.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.7.0" classifier "models"
//  "ch.qos.logback" % "logback-classic" % "1.2.3",
//  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"

mainClass := Some("TwitterFeed")
