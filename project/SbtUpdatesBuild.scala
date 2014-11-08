import sbt._

object SbtUpdatesBuild extends Build {

  lazy val publishMinJar = taskKey[File]("")
  val Embedded = config("embedded").hide

  lazy val `sbt-updates` = project in file(".") settings (
    Project.defaultSettings ++
    inConfig(Embedded)(Defaults.configSettings): _*
  ) configs Embedded

}
