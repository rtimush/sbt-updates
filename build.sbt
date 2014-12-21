sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.8-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.2" % "test"
)

scriptedSettings

