package com.timushev.sbt.updates

import sbt.ModuleID
import semverfi.{PreReleaseVersion, Version, SemVersion}
import dispatch.Promise

object UpdatesFinder {

  def findUpdates(loaders: Seq[MetadataLoader])(module: ModuleID): Promise[Set[SemVersion]] = {
    val current = Version(module.revision)
    val versionSets = loaders map (_ getVersions module recover withEmpty)
    val versions = Promise all versionSets map (_.flatten.toSet)
    versions map (_ filter isUpdate(current) filterNot lessStable(current))
  }

  private def lessStable(current: SemVersion): SemVersion => Boolean = {
    if (isSnapshot(current)) (_ => false) else isSnapshot
  }

  private val isSnapshot: SemVersion => Boolean = {
    case PreReleaseVersion(_, _, _, c) => c.lastOption exists (_ endsWith "SNAPSHOT")
    case _ => false
  }

  private def isUpdate(current: SemVersion) = current < _

  private val withEmpty: PartialFunction[Throwable, Set[SemVersion]] = {
    case _ => Set.empty
  }

}
