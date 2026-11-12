crossScalaVersions := Nil

libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0"

InputKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (!updates.keys.exists(m => m.name == "cats-core"))
    sys.error(s"Missing dependency update for a project with crossScalaVersions := Nil: ${updates.keySet}")
  ()
}
