package com.timushev.sbt.updates

import sbt._
import Keys._
import com.timushev.sbt.updates.UpdatesKeys._

object UpdatesPlugin extends Plugin with UpdatesPluginTasks {

  override val projectSettings = Seq(
    dependencyUpdatesReportFile := target.value / "dependency-updates.txt",
    dependencyUpdatesDataTask,
    dependencyUpdatesTask,
    dependencyUpdatesReportTask
  )

}
