ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

val akkaVersion = "2.6.20"

lazy val root = (project in file("."))
  .settings(
    name := "DistributedAlgorithms"
  ) enablePlugins(CinnamonAgentOnly)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  Cinnamon.library.cinnamonAkkaTyped,
  Cinnamon.library.cinnamonPrometheus,
  Cinnamon.library.cinnamonPrometheusHttpServer,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.scalaz" %% "scalaz-core" % "7.2.35",
  "commons-codec" % "commons-codec" % "1.16.0"
)

updateOptions := updateOptions.value.withGigahorse(false)

// Add the Cinnamon Agent for run and test
run / cinnamon := true
test / cinnamon := true