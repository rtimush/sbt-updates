sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.6-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

def versions(sbtVersion: String) = {
  val V013 = """0\.13(?:\..*|)""".r
  sbtVersion match {
    case V013() => ("7.1.0-M6", "2.1.4-SNAP1")
    case _      => ("7.0.6",    "2.1.4-SNAP1")
  }
}

libraryDependencies ++= {
  val sbtV = (sbtVersion in sbtPlugin).value
  val (scalaz, scalatest) = versions(sbtV)
  Seq(
    "org.scalaz"    %% "scalaz-concurrent" % scalaz    % "embedded",
    "org.scalatest" %% "scalatest"         % scalatest % "test")
}

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")
