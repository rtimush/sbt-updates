package com.timushev.sbt.updates

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._

object UpdatesPlugin extends AutoPlugin {

  object autoImport extends UpdatesKeys

  override val trigger: PluginTrigger = allRequirements

  override val projectSettings = Seq(
    dependencyUpdatesReportFile := target.value / "dependency-updates.txt",
    dependencyUpgradeBuildFile := baseDirectory.value / "build.sbt",
    dependencyUpdatesExclusions := DependencyFilter.fnToModuleFilter(_ => false),
    dependencyUpdatesFailBuild := false,
    dependencyAllowPreRelease := false,
    dependencyUpdatesData := {
      Reporter.dependencyUpdatesData(projectID.value, libraryDependencies.value, fullResolvers.value, credentials.value, crossScalaVersions.value, dependencyUpdatesExclusions.value, dependencyAllowPreRelease.value, streams.value)
    },
    dependencyUpdates := {
      Reporter.displayDependencyUpdates(projectID.value, dependencyUpdatesData.value, dependencyUpdatesFailBuild.value, streams.value)
    },
    dependencyUpdatesReport := {
      Reporter.writeDependencyUpdatesReport(projectID.value, dependencyUpdatesData.value, dependencyUpdatesReportFile.value, streams.value)
    },
    upgradeBuildFile := {
      Reporter.upgradeDependencyUpdates(projectID.value, dependencyUpdatesData.value, dependencyUpgradeBuildFile.value, streams.value)
    }
  )

}
