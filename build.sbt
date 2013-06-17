sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.1-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
    "org.slf4j" % "slf4j-nop" % "1.7.2",
    "org.scalatest" %% "scalatest" % "2.0.M5" % "test")

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12")
