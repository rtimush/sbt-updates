scalaVersion := "2.12.6"

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (updates.keySet != Set(ModuleID("org.scala-lang", "scala-library", "2.12.6")))
    sys.error(s"Wrong update keys: ${updates.keySet}")
  ()
}
