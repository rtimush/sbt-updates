package com.timushev.sbt.updates

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.immutable.SortedSet

trait UpdatesKeys {
  lazy val dependencyUpdatesReportFile = settingKey[File]("Dependency updates report file")
  lazy val dependencyUpdatesExclusions = settingKey[ModuleFilter]("Dependencies that are excluded from update reporting")
  lazy val dependencyUpdatesInclusions = settingKey[ModuleFilter]("Dependencies that are included from update reporting")
  lazy val dependencyUpdatesFailBuild = settingKey[Boolean]("Fail a build if updates found")
  lazy val dependencyAllowPreRelease = settingKey[Boolean]("If true, also take pre-release versions into consideration")
  lazy val dependencyUpdatesData = taskKey[Map[ModuleID, SortedSet[Version]]]("")
  lazy val dependencyUpdates = taskKey[Unit]("Shows a list of project dependencies that can be updated.")
  lazy val dependencyUpdatesReport = taskKey[File]("Writes a list of project dependencies that can be updated to a file.")
}

object UpdatesKeys extends UpdatesKeys
