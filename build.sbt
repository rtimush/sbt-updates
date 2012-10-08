sbtPlugin := true

name := "sbt-updates-plugin"

organization := "com.timushev.sbt"

version := "0.1.0-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
    "me.lessis" %% "semverfi" % "0.1.2",
    "net.databinder.dispatch" %% "dispatch-core" % "0.9.2",
    "org.slf4j" % "slf4j-nop" % "1.6.6",
    "org.scalatest" % "scalatest_2.9.2" % "2.0.M4")

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.11.3", "0.11.2")
