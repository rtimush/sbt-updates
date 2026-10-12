package com.timushev.sbt.updates

import com.timushev.sbt.updates.versions.Version
import sbt._
import sbt.librarymanagement.ModuleFilter

import scala.collection.immutable.SortedSet

trait UpdatesKeys {
  lazy val dependencyUpdatesReportFile    = settingKey[File]("Dependency updates report file")
  lazy val dependencyUpdatesCsvReportFile = settingKey[File]("Dependency updates CSV report file")
  lazy val dependencyUpdatesFilter    = settingKey[ModuleFilter]("Dependencies that are included to update reporting")
  lazy val dependencyUpdatesFailBuild = settingKey[Boolean]("Fail a build if updates found")
  lazy val dependencyAllowPreRelease = settingKey[Boolean]("If true, also take pre-release versions into consideration")
  lazy val dependencyUpdatesData     =
    taskKey[Map[ModuleID, SortedSet[Version]]]("Updates available for each project dependency")
  lazy val dependencyUpdates       = taskKey[Unit]("Shows a list of project dependencies that can be updated.")
  lazy val dependencyUpdatesReport =
    taskKey[File]("Writes a list of project dependencies that can be updated to a file.")
  lazy val dependencyUpdatesCsvReport =
    taskKey[File]("Writes a list of project dependencies that can be updated to a CSV file.")
}

object UpdatesKeys extends UpdatesKeys
