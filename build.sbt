sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.1-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.9.2",
    "org.slf4j" % "slf4j-nop" % "1.6.6",
    "org.scalatest" % "scalatest_2.9.2" % "2.0.M4" % "test")

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.11.3", "0.11.2")
