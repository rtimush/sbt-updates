sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.7-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies <++= (sbtVersion in sbtPlugin) { version =>
  val V013 = """0\.13(?:\..*|)""".r
  val (scalaz, scalatest) = version match {
    case V013() => ("7.1.0-M4", "2.0.1-SNAP4")
    case _ => ("7.0.5", "2.0.M6-SNAP3")
  }
  Seq(
    "org.scalaz"    %% "scalaz-concurrent" % scalaz    % "embedded",
    "org.scalatest" %% "scalatest"         % scalatest % "test")
}

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")
