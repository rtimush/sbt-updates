package com.timushev.sbt.updates

import sbt._
import com.timushev.sbt.updates.UpdatesKeys._

object UpdatesPlugin extends Plugin with UpdatesPluginTasks {


  override val projectSettings = Seq(
    dependencyUpdatesData <<= dependencyUpdatesDataTask,
    dependencyUpdates <<= dependencyUpdatesTask
  )

}
