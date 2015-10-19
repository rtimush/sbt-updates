sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.9"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "com.github.marklister" %% "base64" % "0.1.1",
  "org.scalatest" %% "scalatest"   % "2.2.2" % "test"
)

scriptedSettings

