package com.timushev.sbt.updates

import sbt.ModuleID
import versions.Version
import scalaz.concurrent._

class FixedMetadataLoader(available: Seq[String]) extends MetadataLoader {
  def getVersions(module: ModuleID): Task[Seq[Version]] =
    Task.now(available.map(Version.apply))
}
