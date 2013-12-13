package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import versions.Version

object UpdatesKeys {
  val dependencyUpdatesData = TaskKey[Map[ModuleID, SortedSet[Version]]]("dependency-updates-data")
  val dependencyUpdates = TaskKey[Unit]("dependency-updates", "Shows a list of project dependencies that can be updated.")
}
