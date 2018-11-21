package com.timushev.sbt.updates

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
    dependencyUpdatesData := {
      Reporter.dependencyUpdatesData(
        projectID.value,
        libraryDependencies.value,
        dependencyOverrides.value,
        UpdatesPluginIvyScala.dependencyUpdatesIvtScala.value,
        dependencyPositions.value,
        fullResolvers.value,
        credentials.value,
        crossScalaVersions.value,
        dependencyUpdatesExclusions.value,
        dependencyUpdatesFilter.value,
        dependencyAllowPreRelease.value,
        streams.value
      )
    },
    dependencyUpdates := {
      Reporter.displayDependencyUpdates(projectID.value,
                                        dependencyUpdatesData.value,
                                        dependencyUpdatesFailBuild.value,
                                        streams.value)
    },
    dependencyUpdatesReport := {
      Reporter.writeDependencyUpdatesReport(projectID.value,
                                            dependencyUpdatesData.value,
                                            dependencyUpdatesReportFile.value,
                                            streams.value)
    }
  )

}
