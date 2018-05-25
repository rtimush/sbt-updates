scalaVersion := "2.12.6"

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (updates.keySet.nonEmpty)
    sys.error(s"Wrong update keys: ${updates.keySet}")
  ()
}
