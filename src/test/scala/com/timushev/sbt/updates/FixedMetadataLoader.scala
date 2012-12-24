package com.timushev.sbt.updates

import sbt.ModuleID
import dispatch.{Http, Promise}
import versions.Version

class FixedMetadataLoader(available: Seq[String]) extends MetadataLoader {
  def getVersions(module: ModuleID): Promise[Seq[Version]] =
    Http.promise(available.map(Version.apply))
}
