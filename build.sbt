ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

val akkaVersion = "2.6.20"

lazy val root = (project in file("."))
  .settings(
    name := "DistributedAlgorithms"
  ) enablePlugins CinnamonAgentOnly

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  Cinnamon.library.cinnamonAkkaTyped,
  Cinnamon.library.cinnamonPrometheus,
  Cinnamon.library.cinnamonPrometheusHttpServer,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.scalaz" %% "scalaz-core" % "7.2.35",
  "commons-codec" % "commons-codec" % "1.16.0"
)
Compile / run / mainClass := Some("cs553.Main")
updateOptions := updateOptions.value.withGigahorse(false)

// Add the Cinnamon Agent for run and test
run / cinnamon := true
test / cinnamon := true
//
//scalacOptions += "-language:postfixOps"
//
//scalacOptions ++= Seq()
//
// This prevents individual tests executing in parallel,
// thus, messing up the ThreadID logic
parallelExecution in ThisBuild := false