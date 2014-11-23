sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.7"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.2" % "test"
)

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.13")

