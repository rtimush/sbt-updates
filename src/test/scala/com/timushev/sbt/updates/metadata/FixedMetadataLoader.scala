package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version
import sbt.ModuleID

import scala.concurrent._

class FixedMetadataLoader(available: Seq[String], val repository: String = "fixed") extends MetadataLoader {
  def getVersions(module: ModuleID): Future[Seq[Version]] = Future.successful(available.map(Version.apply))
}
