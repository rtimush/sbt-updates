package com.timushev.sbt.updates

import sbt._
import scala.collection.immutable.SortedSet
import versions.Version

object UpdatesKeys {
  val dependencyUpdatesData = TaskKey[Map[ModuleID, SortedSet[Version]]]("dependencyUpdatesData")
  val dependencyUpdates = TaskKey[Unit]("dependencyUpdates")
}
