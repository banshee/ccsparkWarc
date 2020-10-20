name := "com.banshee.ccsparkWarc"

version := "0.1"

scalacOptions := Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("palantir", "releases")
)

scalaVersion := "2.12.12"

libraryDependencies += "com.google.guava" % "guava" % "11+"
libraryDependencies += "com.jsuereth" %% "scala-arm" % "2.0"
//libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4" % "provided"
//libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.4" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0" % "provided"
//libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "2.4.5" % "provided"
libraryDependencies += "org.netpreserve" % "jwarc" % "0.9.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "net.liftweb" %% "lift-json" % "3.4.1"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "1.4.10"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.5"
libraryDependencies += "org.freemarker" % "freemarker" % "2.3.30"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-core" % "1.11.791"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.791"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.791"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.791"
//libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.2.1"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-shapes",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

scalacOptions += "-Ypartial-unification"

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)
