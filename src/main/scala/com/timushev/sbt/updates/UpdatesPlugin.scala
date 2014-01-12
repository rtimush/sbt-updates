package com.timushev.sbt.updates

import sbt._
import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

object UpdatesPlugin extends Plugin with UpdatesPluginTasks {

  override val projectSettings = Seq(
    dependencyUpdatesReportFile <<= target apply (_ / "dependency-updates.txt"),
    dependencyUpdatesData <<= dependencyUpdatesDataTask,
    dependencyUpdates <<= dependencyUpdatesTask,
    dependencyUpdatesReport <<= writeDependencyUpdatesReportTask
  )

}
