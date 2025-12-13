package com.timushev.sbt.updates

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._
import com.timushev.sbt.updates.Compat._
import com.timushev.sbt.updates.DependencyPositions.dependencyPositionsTask
import com.timushev.sbt.updates.authentication.Coursier.{dependencyUpdatesCsrConfigurationTask, CoursierConfiguration}
import com.timushev.sbt.updates.model.{Csv, SbtOutput}

object UpdatesPlugin extends AutoPlugin {
  object autoImport extends UpdatesKeys with Implicits

  override val trigger: PluginTrigger = allRequirements

  private val dependencyUpdatesCsrConfiguration = taskKey[Option[CoursierConfiguration]]("")

  override val projectSettings = Seq(
    dependencyUpdatesReportFile       := target.value / "dependency-updates.txt",
    dependencyUpdatesExclusions       := DependencyFilter.fnToModuleFilter(_ => false),
    dependencyUpdatesFilter           := DependencyFilter.fnToModuleFilter(_ => true),
    dependencyUpdatesFailBuild        := false,
    dependencyAllowPreRelease         := false,
    dependencyUpdatesCsrConfiguration := dependencyUpdatesCsrConfigurationTask.value,
    dependencyUpdatesData             :=
      Reporter.dependencyUpdatesData(
        libraryDependencies.value,
        dependencyOverrides.value,
        dependencyPositionsTask.value,
        fullResolvers.value,
        credentials.value,
        dependencyUpdatesCsrConfiguration.value,
        crossScalaVersions.value,
        dependencyUpdatesExclusions.value,
        dependencyUpdatesFilter.value,
        dependencyAllowPreRelease.value,
        (ThisBuild / baseDirectory).value,
        streams.value
      ),
    dependencyUpdates :=
      Reporter.displayDependencyUpdates(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesFailBuild.value,
        streams.value
      ),
    dependencyUpdatesReport :=
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesReportFile.value,
        SbtOutput,
        streams.value
      ),
    dependencyUpdatesCsvReport :=
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesReportFile.value,
        Csv,
        streams.value
      )
  )
}
