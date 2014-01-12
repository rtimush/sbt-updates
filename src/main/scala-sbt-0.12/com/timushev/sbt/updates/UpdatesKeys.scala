package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import versions.Version

object UpdatesKeys {
  val dependencyUpdatesReportFile = SettingKey[File]("dependency-updates-report-file")
  val dependencyUpdatesData = TaskKey[Map[ModuleID, SortedSet[Version]]]("dependency-updates-data")
  val dependencyUpdates = TaskKey[Unit]("dependency-updates", "Shows a list of project dependencies that can be updated.")
  val dependencyUpdatesReport = TaskKey[File]("dependency-updates-report", "Writes a list of project dependencies that can be updated to a file.")
}
