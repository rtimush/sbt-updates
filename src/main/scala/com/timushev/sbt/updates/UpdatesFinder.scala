package com.timushev.sbt.updates

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.metadata.MetadataLoader
import com.timushev.sbt.updates.versions._
import sbt.ModuleID

import java.io.FileNotFoundException
import scala.collection.immutable.SortedSet
import scala.concurrent.Future
import scala.util.control.NonFatal

object UpdatesFinder {
  import scala.Ordered._

  final case class LookupFailure(repository: String, module: ModuleID, cause: Throwable)
  final case class ModuleUpdates(versions: SortedSet[Version], failures: Seq[LookupFailure])

  def findUpdates(loaders: Seq[MetadataLoader], allowPreRelease: Boolean)(
      module: ModuleID
  ): Future[ModuleUpdates] = {
    val current = Version(module.revision)
    val lookups = loaders.map { loader =>
      Future.unit
        .flatMap(_ => loader.getVersions(module)) // a loader failing before it returns a Future fails like any other
        .map(versions => ModuleUpdates(SortedSet.empty[Version] ++ versions, Nil))
        .recover {
          case _: FileNotFoundException => ModuleUpdates(SortedSet.empty, Nil)
          case NonFatal(e) => ModuleUpdates(SortedSet.empty, Seq(LookupFailure(loader.repository, module, e)))
        }
    }
    Future.sequence(lookups).map { results =>
      val versions = results.foldLeft(SortedSet.empty[Version])(_ ++ _.versions)
      ModuleUpdates(
        versions.filter(isUpdate(current)).filterNot(lessStable(current, allowPreRelease)),
        results.flatMap(_.failures)
      )
    }
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

  private def isUpdate(current: Version) = (another: Version) => current < another
}
