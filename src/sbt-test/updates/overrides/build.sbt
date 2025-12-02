import com.timushev.sbt.updates.versions.Version

scalaVersion       := "2.10.4"
crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies += "org.specs2" %% "specs2" % "3.1"
dependencyOverrides += "org.specs2" %% "specs2" % "3.1.1"

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  val found   = updates.keys.exists {
    case m if m.organization == "org.scala-lang" => false
    case m if m.organization == "org.specs2"     =>
      val versions = updates(m)
      if (versions.contains(Version("3.1.1")))
        sys.error(s"Wrong update versions: $versions, should not contain 3.1.1")
      if (!versions.contains(Version("3.2")))
        sys.error(s"Wrong update versions: $versions, should contain 3.2")
      true
    case other =>
      sys.error(s"Wrong update key: $other")
      false
  }
  if (!found)
    sys.error("No updates for specs2 found")
  ()
}
