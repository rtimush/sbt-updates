import foo._

lazy val subproject = project
  .in(file("subproject"))
  .settings(PackageDependencies.deps)
  .settings(RootDependencies.deps)
  .settings(bar.NestedDependencies.deps)

PackageDependencies.deps
RootDependencies.deps

InputKey[Unit]("check") := {
  val subprojectUpdates = (subproject / dependencyUpdatesData).value
  val updates           = dependencyUpdatesData.value
  if (!subprojectUpdates.keys.exists(m => m.name == "cats-core"))
    sys.error(
      s"Missing dependency update for a subproject dependency defined in project/ in a non-default package: ${subprojectUpdates.keySet}"
    )
  if (!subprojectUpdates.keys.exists(m => m.organization == "org.scalatest"))
    sys.error(
      s"Missing dependency update for a subproject dependency defined in project/ in a default package: ${subprojectUpdates.keySet}"
    )
  if (!subprojectUpdates.keys.exists(m => m.name == "cats-kernel"))
    sys.error(
      s"Missing dependency update for a subproject dependency defined in project/src/main/scala: ${subprojectUpdates.keySet}"
    )
  if (!updates.keys.exists(m => m.name == "cats-core"))
    sys.error(
      s"Missing dependency update for a root project dependency defined in project/ in a non-default package: ${updates.keySet}"
    )
  if (!updates.keys.exists(m => m.organization == "org.scalatest"))
    sys.error(
      s"Missing dependency update for a root project dependency defined in project/ in a default package: ${updates.keySet}"
    )
  ()
}
