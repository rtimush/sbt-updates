import sbt._
import Keys._

object SbtUpdatesBuild extends Build {

  val Embedded = config("embedded") hide

  lazy val `sbt-updates` = project in file(".") settings(
    Project.defaultSettings ++
      inConfig(Embedded)(Defaults.configSettings): _*
  ) configs Embedded

  val V013 = """0\.13(?:\..*|)""".r

  def scalazVersion(sbtVersion: String): String = {
    sbtVersion match {
      case V013() => "7.1.0-M6"
      case _      => "7.0.6"
    }
  }

  def sbtDependentSrcs(sbtVersion: String, srcMainDir: java.io.File): java.io.File = {
    sbtVersion match {
      case V013() => srcMainDir / "scala-sbt-0.13"
      case _      => srcMainDir / "scala-sbt-0.12"
    }    
  }
}
