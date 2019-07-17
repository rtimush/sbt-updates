import foo._

lazy val subproject = project.in(file("subproject"))
  .settings(PackageDependencies.deps)
  .settings(RootDependencies.deps)

PackageDependencies.deps
RootDependencies.deps

TaskKey[Unit]("check") := {
  val subprojectUpdates = (dependencyUpdatesData in subproject).value
  val updates = dependencyUpdatesData.value
  if (!subprojectUpdates.keys.exists(m => m.organization == "org.specs2"))
    sys.error(s"Missing dependency update for a subproject dependency defined in project/ in a non-default package: ${subprojectUpdates.keySet}")
  if (!subprojectUpdates.keys.exists(m => m.organization == "org.scalatest"))
    sys.error(s"Missing dependency update for a subproject dependency defined in project/ in a default package: ${subprojectUpdates.keySet}")
  if (!updates.keys.exists(m => m.organization == "org.specs2"))
    sys.error(s"Missing dependency update for a root project dependency defined in project/ in a non-default package: ${updates.keySet}")
  if (!updates.keys.exists(m => m.organization == "org.scalatest"))
    sys.error(s"Missing dependency update for a root project dependency defined in project/ in a default package: ${updates.keySet}")
  ()
}
