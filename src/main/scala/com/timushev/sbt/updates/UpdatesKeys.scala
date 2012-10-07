package com.timushev.sbt.updates

import sbt._

object UpdatesKeys {
  val dependencyUpdates = TaskKey[Unit]("dependency-updates")
}
