package com.timushev.sbt.updates

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._
import sbt.librarymanagement.DependencyFilter
import sbt.librarymanagement.ModuleFilter
import com.timushev.sbt.updates.DependencyPositions.dependencyPositionsTask
import com.timushev.sbt.updates.authentication.Coursier.{dependencyUpdatesCsrConfigurationTask, CoursierConfiguration}
import com.timushev.sbt.updates.model.{Csv, SbtOutput}
import com.timushev.sbt.updates.upgrade.Upgrader

object UpdatesPlugin extends AutoPlugin {
  object autoImport extends UpdatesKeys with Implicits

  override val trigger: PluginTrigger = allRequirements

  private val dependencyUpdatesCsrConfiguration = taskKey[Option[CoursierConfiguration]]("")

  // Bundles each project's id, declared dependencies and updates into one value, so that `.all` over the aggregated
  // projects keeps them paired (separate `.all` calls are not guaranteed to list projects in the same order or number).
  // `dependencyUpdatesUpgrade` needs the pairing to stop at the latest release every project sharing a declaration can
  // use, including projects that declare a dependency but have no newer version of it.
  private val dependencyUpdatesUpgradeProject = taskKey[Upgrader.ProjectUpdates](
    "Internal: a project's id, dependencies and updates, gathered by dependencyUpdatesUpgrade across aggregated projects."
  )

  override val projectSettings = Seq(
    dependencyUpdatesReportFile       := target.value / "dependency-updates.txt",
    dependencyUpdatesExclusions       := DependencyFilter.fnToModuleFilter(_ => false),
    dependencyUpdatesFilter           := DependencyFilter.fnToModuleFilter(_ => true),
    dependencyUpdatesFailBuild        := false,
    dependencyAllowPreRelease         := false,
    dependencyUpdatesCsrConfiguration := Compat.uncached(dependencyUpdatesCsrConfigurationTask.value),
    dependencyUpdatesData             := Compat.uncached(
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
      )
    ),
    dependencyUpdates := Compat.uncached(
      Reporter.displayDependencyUpdates(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesFailBuild.value,
        streams.value
      )
    ),
    dependencyUpdatesReport := Compat.uncached(
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesReportFile.value,
        SbtOutput,
        streams.value
      )
    ),
    dependencyUpdatesCsvReport := Compat.uncached(
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesReportFile.value,
        Csv,
        streams.value
      )
    ),
    dependencyUpdatesUpgradeProject := Compat.uncached(
      Upgrader.ProjectUpdates(thisProject.value.id, libraryDependencies.value, dependencyUpdatesData.value)
    ),
    dependencyUpdatesUpgrade := Compat.uncached(
      Upgrader.upgrade(
        dependencyUpdatesUpgradeProject.all(ScopeFilter(inAggregates(ThisProject))).value,
        (ThisBuild / baseDirectory).value,
        baseDirectory.all(ScopeFilter(inAnyProject)).value,
        streams.value.log
      )
    ),
    // a single task instance changes the build sources for all aggregated projects at once
    dependencyUpdatesUpgrade / aggregate := false
  )
}
