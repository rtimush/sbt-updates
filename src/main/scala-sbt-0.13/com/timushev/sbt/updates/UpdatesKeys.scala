package com.timushev.sbt.updates

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.immutable.SortedSet

object UpdatesKeys {
  lazy val dependencyUpdatesReportFile = settingKey[File]("")
  lazy val dependencyUpdatesData = taskKey[Map[ModuleID, SortedSet[Version]]]("")
  lazy val dependencyUpdates = taskKey[Unit]("Shows a list of project dependencies that can be updated.")
  lazy val dependencyUpdatesReport = taskKey[File]("Writes a list of project dependencies that can be updated to a file.")
}
