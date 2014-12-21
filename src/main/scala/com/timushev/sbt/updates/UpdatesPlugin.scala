package com.timushev.sbt.updates

import sbt._
import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

object UpdatesPlugin extends AutoPlugin with UpdatesPluginTasks {

  object autoImport extends UpdatesKeys

  override val trigger = allRequirements

  override val projectSettings = Seq(
    dependencyUpdatesReportFile := target.value / "dependency-updates.txt",
    dependencyUpdatesExclusions := DependencyFilter.fnToModuleFilter(_ => false),
    dependencyUpdatesFailBuild := false,
    dependencyUpdatesData <<= dependencyUpdatesDataTask,
    dependencyUpdates <<= dependencyUpdatesTask,
    dependencyUpdatesReport <<= writeDependencyUpdatesReportTask
  )

}
