sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.7-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies <++= (sbtVersion in sbtPlugin) { version =>
  val V013 = """0\.13(?:\..*|)""".r
  val (scalaz, scalatest) = version match {
    case V013() => ("7.1.0", "2.2.2")
  }
  Seq(
    "org.scalaz"    %% "scalaz-concurrent" % scalaz    % "embedded",
    "org.scalatest" %% "scalatest"         % scalatest % "test")
}

CrossBuilding.settings

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.13")
