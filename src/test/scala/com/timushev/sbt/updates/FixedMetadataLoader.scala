package com.timushev.sbt.updates

import sbt.ModuleID
import dispatch.Promise
import versions.Version

class FixedMetadataLoader(available: Seq[String]) extends MetadataLoader {
  def getVersions(module: ModuleID): Promise[Seq[Version]] =
    Promise(available.map(Version.apply))
}
