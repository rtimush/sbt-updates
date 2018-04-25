package com.timushev.sbt.updates

import scala.concurrent.duration._

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._
import com.timushev.sbt.updates.Compat._

object UpdatesPlugin extends AutoPlugin {

  object autoImport extends UpdatesKeys with Implicits

  override val trigger: PluginTrigger = allRequirements

  override val projectSettings = Seq(
    dependencyUpdatesReportFile := target.value / "dependency-updates.txt",
    dependencyUpdatesExclusions := DependencyFilter.fnToModuleFilter(_ => false),
    dependencyUpdatesFilter := DependencyFilter.fnToModuleFilter(_ => true),
    dependencyUpdatesFailBuild := false,
    dependencyAllowPreRelease := false,
    dependencyUpdatesTimeout := 1.hour,
    dependencyUpdatesData := {
      Reporter.dependencyUpdatesData(projectID.value, libraryDependencies.value, fullResolvers.value, credentials.value, crossScalaVersions.value, dependencyUpdatesExclusions.value, dependencyUpdatesFilter.value, dependencyAllowPreRelease.value, dependencyUpdatesTimeout.value, streams.value)
    },
    dependencyUpdates := {
      Reporter.displayDependencyUpdates(projectID.value, dependencyUpdatesData.value, dependencyUpdatesFailBuild.value, streams.value)
    },
    dependencyUpdatesReport := {
      Reporter.writeDependencyUpdatesReport(projectID.value, dependencyUpdatesData.value, dependencyUpdatesReportFile.value, streams.value)
    }
  )

}
