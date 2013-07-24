package com.timushev.sbt.updates

import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

trait UpdatesPluginTasks {

  def dependencyUpdatesDataTask =
    (projectID, libraryDependencies, externalResolvers, scalaVersion, scalaBinaryVersion)
      .map(Reporter.dependencyUpdatesData)

  def dependencyUpdatesTask =
    (projectID, dependencyUpdatesData, streams)
      .map(Reporter.displayDependencyUpdates)

}
