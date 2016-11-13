package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.concurrent.Future

trait MetadataLoader {
  def getVersions(module: ModuleID): Future[Seq[Version]]
}
