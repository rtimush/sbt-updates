package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import semverfi.SemVersion

object UpdatesKeys {
  val dependencyUpdatesData = TaskKey[Map[ModuleID, SortedSet[SemVersion]]]("dependency-updates-data")
  val dependencyUpdates = TaskKey[Unit]("dependency-updates")
}
