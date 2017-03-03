import com.timushev.sbt.updates.versions.Version

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

dependencyUpdatesInclusions := moduleFilter(organization = "org.scala-lang", name = "scala-reflect", revision = "2.10.5")

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (updates.keySet != Set(ModuleID("org.scala-lang", "scala-reflect", "2.10.4")))
    sys.error(s"Wrong update keys: ${updates.keySet}")
  val versions = updates(updates.keys.head)
  if (!versions.contains(Version("2.10.5")))
    sys.error(s"Wrong update versions: $versions")
  ()
}
