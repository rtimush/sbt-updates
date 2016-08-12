package com.timushev.sbt.updates

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._

trait UpdatesPluginTasks {

  def dependencyUpdatesDataTask =
    (projectID, libraryDependencies, externalResolvers, credentials, crossScalaVersions, dependencyUpdatesExclusions, dependencyAllowPreRelease, streams)
      .map(Reporter.dependencyUpdatesData)

  def dependencyUpdatesTask =
    (projectID, dependencyUpdatesData, dependencyUpdatesFailBuild, streams)
      .map(Reporter.displayDependencyUpdates)

  def writeDependencyUpdatesReportTask =
    (projectID, dependencyUpdatesData, dependencyUpdatesReportFile, streams)
      .map(Reporter.writeDependencyUpdatesReport)

}
