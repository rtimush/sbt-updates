package com.timushev.sbt.updates

import sbt._
import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

object UpdatesPlugin extends Plugin with UpdatesPluginTasks with UpdatesKeys {

  override val projectSettings = Seq(
    dependencyUpdatesReportFile := target.value / "dependency-updates.txt",
    dependencyUpdatesFailBuild := false,
    dependencyUpdatesData <<= dependencyUpdatesDataTask,
    dependencyUpdates <<= dependencyUpdatesTask,
    dependencyUpdatesReport <<= writeDependencyUpdatesReportTask
  )

}
