import sbt._

object SbtUpdatesBuild extends Build {

  val publishMinJar = TaskKey[File]("publish-min-jar")
  val Embedded = config("embedded").hide

  lazy val root = Project(id = "sbt-updates", base = file("."), settings = Project.defaultSettings)
    .configs(Embedded)
    .settings(inConfig(Embedded)(Defaults.configSettings): _*)

}
