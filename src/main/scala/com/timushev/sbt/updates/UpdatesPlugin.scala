package com.timushev.sbt.updates

import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._
import sbt.librarymanagement.DependencyFilter
import com.timushev.sbt.updates.DependencyPositions.dependencyPositionsTask
import com.timushev.sbt.updates.authentication.Coursier.dependencyUpdatesCsrConfigurationTask
import com.timushev.sbt.updates.model.{Csv, SbtOutput}

object UpdatesPlugin extends AutoPlugin {
  object autoImport extends UpdatesKeys with Implicits

  override val trigger: PluginTrigger = allRequirements

  private val dependencyUpdatesCsrConfiguration =
    taskKey[Option[AnyRef]]("Coursier configuration, read for repository credentials")
  private val dependencyUpdatesSession =
    taskKey[LookupSession]("Lookup state shared by all projects within one command")

  override val globalSettings = Seq(
    dependencyUpdatesSession := Compat.uncached(new LookupSession)
  )

  override val projectSettings = Seq(
    dependencyUpdatesReportFile       := target.value / "dependency-updates.txt",
    dependencyUpdatesCsvReportFile    := target.value / "dependency-updates.csv",
    dependencyUpdatesFilter           := DependencyFilter.fnToModuleFilter(_ => true),
    dependencyUpdatesFailBuild        := false,
    dependencyAllowPreRelease         := false,
    dependencyUpdatesCsrConfiguration := Compat.uncached(dependencyUpdatesCsrConfigurationTask.value),
    dependencyUpdatesData             := Compat.uncached(
      Reporter.dependencyUpdatesData(
        projectID.value,
        libraryDependencies.value,
        dependencyOverrides.value,
        dependencyPositionsTask.value,
        fullResolvers.value,
        credentials.value,
        dependencyUpdatesCsrConfiguration.value,
        Reporter.scalaVersionsToCheck(scalaVersion.value, crossScalaVersions.value),
        dependencyUpdatesFilter.value,
        dependencyAllowPreRelease.value,
        (ThisBuild / baseDirectory).value,
        (Global / dependencyUpdatesSession).value,
        streams.value.log
      )
    ),
    dependencyUpdates := Compat.uncached(
      Reporter.displayDependencyUpdates(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesFailBuild.value,
        streams.value.log
      )
    ),
    dependencyUpdatesReport := Compat.uncached(
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesReportFile.value,
        SbtOutput,
        streams.value.log
      )
    ),
    dependencyUpdatesCsvReport := Compat.uncached(
      Reporter.writeDependencyUpdatesReport(
        projectID.value,
        dependencyUpdatesData.value,
        dependencyUpdatesCsvReportFile.value,
        Csv,
        streams.value.log
      )
    )
  )
}
