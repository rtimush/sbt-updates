sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.1-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-concurrent" % "7.0.0",
    "org.scalatest" %% "scalatest" % "2.0.M5" % "test"
)

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12")
