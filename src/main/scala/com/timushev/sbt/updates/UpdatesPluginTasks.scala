package com.timushev.sbt.updates

import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

trait UpdatesPluginTasks {

  def dependencyUpdatesDataTask =
    (projectID, libraryDependencies, externalResolvers, scalaVersion, scalaBinaryVersion, dependencyUpdatesExclusions, streams)
      .map(Reporter.dependencyUpdatesData)

  def dependencyUpdatesTask =
    (projectID, dependencyUpdatesData, dependencyUpdatesFailBuild, streams)
      .map(Reporter.displayDependencyUpdates)

  def writeDependencyUpdatesReportTask =
    (projectID, dependencyUpdatesData, dependencyUpdatesReportFile, streams)
      .map(Reporter.writeDependencyUpdatesReport)

}
