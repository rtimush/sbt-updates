scalaVersion := "2.12.6"

InputKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  val keys   = updates.keys.toSeq
  if (keys.size != 1) sys.error(s"Wrong update keys: ${updates.keySet}")
  val m = keys.head
  if (m.organization != "org.scala-lang" || m.name != "scala-library" || m.revision != "2.12.6")
    sys.error(s"Wrong update keys: ${updates.keySet}")
  ()
}
