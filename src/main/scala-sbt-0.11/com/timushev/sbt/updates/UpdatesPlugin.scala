package com.timushev.sbt.updates

import sbt._

object UpdatesPlugin extends Plugin with UpdatesPluginTasks {

  import UpdatesKeys._

  override val settings = Seq(
    dependencyUpdatesData <<= dependencyUpdatesDataTask,
    dependencyUpdates <<= dependencyUpdatesTask
  )

}
