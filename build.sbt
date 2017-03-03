sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:experimental.macros",
  "-language:postfixOps",
  "-unchecked",
  "-Ywarn-nullary-unit",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yinline-warnings",
  "-Ywarn-dead-code",
  "-Xfuture"
)

resolvers ++= Seq(
  "Curalate snapshots" at "https://maven.curalate.com/content/repositories/snapshots/",
  "Curalate releases" at "https://maven.curalate.com/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

scriptedSettings
