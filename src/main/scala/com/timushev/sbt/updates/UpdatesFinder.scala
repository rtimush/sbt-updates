package com.timushev.sbt.updates

import com.timushev.sbt.updates.metadata.MetadataLoader
import com.timushev.sbt.updates.versions._
import sbt.ModuleID

import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UpdatesFinder {

  import scala.Ordered._

  def findUpdates(loaders: Seq[MetadataLoader], allowPreRelease: Boolean)(
      module: ModuleID
  ): Future[SortedSet[Version]] = {
    val current = Version(module.revision)
    val versionSets = loaders.map(_.getVersions(module).recover(withEmpty))
    val versions = Future.sequence(versionSets).map(v => SortedSet(v.flatten: _*))
    versions.map(_.filter(isUpdate(current)).filterNot(lessStable(current, allowPreRelease)))
  }

  private def lessStable(current: Version, allowPreRelease: Boolean)(another: Version): Boolean =
    (current, another) match {
      case (ReleaseVersion(_), ReleaseVersion(_))               => false
      case (SnapshotVersion(_, _, _), _)                        => false
      case (_, SnapshotVersion(_, _, _))                        => true
      case (ReleaseVersion(_), PreReleaseVersion(_, _))         => !allowPreRelease
      case (ReleaseVersion(_), PreReleaseBuildVersion(_, _, _)) => !allowPreRelease
      case (ReleaseVersion(_), _)                               => true
      case (_, _)                                               => false
    }

  private def isUpdate(current: Version) = current < _

  private val withEmpty: PartialFunction[Throwable, Seq[Version]] = {
    case _ => Seq.empty
  }

}
