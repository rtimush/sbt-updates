package com.timushev.sbt.updates

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.immutable.SortedSet
import com.timushev.sbt.updates.Compat._

trait UpdatesKeys {
  lazy val dependencyUpdatesReportFile = settingKey[File]("Dependency updates report file")
  @deprecated(
    "dependencyUpdatesExclusions is deprecated in favor of dependencyUpdatesFilter, which defaults" + " to a truthy check. Migrate exclusions by setting dependencyUpdatesFilter -= yourExclusions",
    "0.4.0"
  )
  lazy val dependencyUpdatesExclusions =
    settingKey[ModuleFilter]("Dependencies that are excluded from update reporting")
  lazy val dependencyUpdatesFilter = settingKey[ModuleFilter]("Dependencies that are included to update reporting")
  lazy val dependencyUpdatesFailBuild = settingKey[Boolean]("Fail a build if updates found")
  lazy val dependencyAllowPreRelease = settingKey[Boolean]("If true, also take pre-release versions into consideration")
  lazy val dependencyUpdatesData = taskKey[Map[ModuleID, SortedSet[Version]]]("")
  lazy val dependencyUpdates = taskKey[Unit]("Shows a list of project dependencies that can be updated.")
  lazy val dependencyUpdatesReport =
    taskKey[File]("Writes a list of project dependencies that can be updated to a file.")
}

object UpdatesKeys extends UpdatesKeys
