sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.7-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.scalaz"    %% "scalaz-core" % "7.1.0" % "embedded",
  "org.scalatest" %% "scalatest"   % "2.2.2" % "test"
)

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.13")
