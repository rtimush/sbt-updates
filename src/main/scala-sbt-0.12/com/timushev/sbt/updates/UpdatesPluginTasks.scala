package com.timushev.sbt.updates

import sbt.Keys._

trait UpdatesPluginTasks {
  def dependencyUpdatesTask =
    (projectID, libraryDependencies, externalResolvers, scalaVersion, scalaBinaryVersion, streams)
      .map(Reporter.displayDependencyUpdates)
}
