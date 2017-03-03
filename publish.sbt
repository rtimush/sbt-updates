lazy val revision = System.getProperty("revision", "SNAPSHOT")

version := s"1.0-$revision"

publishMavenStyle := true

publishTo := {
  val nexus = "https://maven.curalate.com/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "content/repositories/releases")
}

lazy val showVersion = taskKey[Unit]("Show version")

showVersion := {
  println(version.value)
}

addCommandAlias("build", "; compile; scripted")

licenses += (("BSD 3-Clause", url("https://github.com/rtimush/sbt-updates/blob/master/LICENSE")))
