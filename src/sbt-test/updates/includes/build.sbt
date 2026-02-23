import com.timushev.sbt.updates.versions.Version

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

dependencyUpdatesFilter := moduleFilter(organization = "org.scala-lang", name = "scala-reflect", revision = "2.10.5")

InputKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  val keys   = updates.keys.toSeq
  if (keys.size != 1) sys.error(s"Wrong update keys: ${updates.keySet}")
  val m = keys.head
  if (m.organization != "org.scala-lang" || m.name != "scala-reflect" || m.revision != "2.10.4")
    sys.error(s"Wrong update keys: ${updates.keySet}")
  val versions = updates(m)
  if (!versions.contains(Version("2.10.5")))
    sys.error(s"Wrong update versions: $versions")
  ()
}
