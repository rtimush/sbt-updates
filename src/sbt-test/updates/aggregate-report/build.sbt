import com.timushev.sbt.updates.versions.Version

import scala.collection.immutable.SortedSet

lazy val root = (project in file("."))
  .aggregate(projectA, projectB)
  .settings(
    scalaVersion := "2.10.4"
  )

lazy val foo = ModuleID("org.example", "foo", "1.0.0")
lazy val bar = ModuleID("org.example", "bar", "2.0.0")
lazy val scalaLang = ModuleID("org.scala-lang", "scala-library", "2.10.4")

lazy val projectA = project
  .settings(
    dependencyUpdatesData := Map(
      foo -> SortedSet(Version("2.0.0"))
    )
  )

lazy val projectB = project
  .settings(
    dependencyUpdatesData := Map(
      foo -> SortedSet(Version("2.0.1")),
      bar -> SortedSet(Version("2.1.0"))
    )
  )

TaskKey[Unit]("check") := {
  val updates = dependencyUpdatesData.value
  if (updates.keySet != Set(scalaLang)) {
    sys.error(s"Wrong update keys: ${updates.keySet}")
  }
  val rootReport = IO.read((dependencyUpdatesReport in LocalRootProject).value)
  val reportA = IO.read((dependencyUpdatesReport in projectA).value)
  val reportB = IO.read((dependencyUpdatesReport in projectB).value)
  val aggregateReport = IO.read(dependencyUpdatesReport.value)
  if (!aggregateReport.contains(rootReport)) {
    sys.error("Root report is not aggregated")
  }
  if (!aggregateReport.contains(reportA)) {
    sys.error("ProjectA report is not aggregated")
  }
  if (!aggregateReport.contains(reportB)) {
    sys.error("ProjectB report is not aggregated")
  }
  ()
}
