import com.timushev.sbt.updates.versions.Version

scalaVersion := "2.10.4"

dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang", revision = "2.10.5")

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (updates.keySet != Set(ModuleID("org.scala-lang", "scala-library", "2.10.4")))
    sys.error(s"Wrong update keys: ${updates.keySet}")
  val versions = updates(updates.keys.head)
  if (versions.contains(Version("2.10.5")))
    sys.error(s"Wrong update versions: $versions")
  ()
}
