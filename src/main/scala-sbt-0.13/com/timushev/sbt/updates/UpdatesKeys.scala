package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import versions.Version

object UpdatesKeys {
  lazy val dependencyUpdatesReportFile = settingKey[File]("dependencyUpdatesReportFile")
  lazy val dependencyUpdatesData = taskKey[Map[ModuleID, SortedSet[Version]]]("dependencyUpdatesData")
  lazy val dependencyUpdates = taskKey[Unit]("Shows a list of project dependencies that can be updated.")
  lazy val dependencyUpdatesReport = taskKey[File]("Writes a list of project dependencies that can be updated to a file.")
}
