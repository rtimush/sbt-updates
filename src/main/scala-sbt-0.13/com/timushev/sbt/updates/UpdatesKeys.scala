package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import versions.Version

object UpdatesKeys {
  val dependencyUpdatesReportFile = SettingKey[File]("dependencyUpdatesReportFile")
  val dependencyUpdatesData = TaskKey[Map[ModuleID, SortedSet[Version]]]("dependencyUpdatesData")
  val dependencyUpdates = TaskKey[Unit]("dependencyUpdates", "Shows a list of project dependencies that can be updated.")
  val dependencyUpdatesReport = TaskKey[File]("dependencyUpdatesReport", "Writes a list of project dependencies that can be updated to a file.")
}
