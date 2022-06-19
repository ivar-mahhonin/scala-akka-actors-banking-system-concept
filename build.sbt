import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.banking"
ThisBuild / organizationName := "example"
val AkkaVersion = "2.6.19"

fork in run := true

lazy val root = (project in file("."))
  .settings(
    name := "test-actors",
    libraryDependencies += scalaTest % Test
  )

  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
