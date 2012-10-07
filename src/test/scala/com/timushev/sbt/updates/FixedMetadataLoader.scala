package com.timushev.sbt.updates

import sbt.ModuleID
import dispatch.Promise
import semverfi.{Version, SemVersion}

class FixedMetadataLoader(available: Seq[String]) extends MetadataLoader {
  def getVersions(module: ModuleID): Promise[Seq[SemVersion]] =
    Promise(available.map(Version.apply))
}
