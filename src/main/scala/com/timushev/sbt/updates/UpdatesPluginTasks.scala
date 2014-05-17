package com.timushev.sbt.updates

import sbt.Keys._
import com.timushev.sbt.updates.UpdatesKeys._

trait UpdatesPluginTasks {

  lazy val dependencyUpdatesDataTask = dependencyUpdatesData := {
    Reporter.dependencyUpdatesData(
    	projectID.value,
    	libraryDependencies.value,
    	externalResolvers.value,
    	scalaVersion.value,
    	scalaBinaryVersion.value
    )
  }

  lazy val dependencyUpdatesTask = dependencyUpdates := {
    Reporter.displayDependencyUpdates(
    	projectID.value,
    	dependencyUpdatesData.value,
    	streams.value
    )
  }

  lazy val dependencyUpdatesReportTask = dependencyUpdatesReport := {
  	Reporter.writeDependencyUpdatesReport(
  		projectID.value,
  		dependencyUpdatesData.value,
  		dependencyUpdatesReportFile.value,
  		streams.value
  	)
  }
}
