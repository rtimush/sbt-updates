sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.3-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies <++= (sbtVersion in sbtPlugin) {
  case "0.12" =>
    Seq(
      "org.scalaz" %% "scalaz-concurrent" % "7.0.5" % "embedded",
      "org.scalatest" %% "scalatest" % "2.0.M6-SNAP3" % "test")
  case "0.13" =>
    Seq(
      "org.scalaz" %% "scalaz-concurrent" % "7.1.0-M4" % "embedded",
      "org.scalatest" %% "scalatest" % "2.0.1-SNAP3" % "test")
}

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")
