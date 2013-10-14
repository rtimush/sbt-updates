package com.timushev.sbt.updates

import sbt.ModuleID
import scala.collection.immutable.SortedSet
import com.timushev.sbt.updates.versions._
import scalaz.concurrent._
import scalaz.syntax.traverse._
import scalaz.std.list._

object UpdatesFinder {

  import Ordered._

  def findUpdates(loaders: Seq[MetadataLoader])(module: ModuleID): Task[SortedSet[Version]] = {
    val current = Version(module.revision)
    val versionSets = loaders map (_ getVersions module handle withEmpty)
    val versions = versionSets.toList.sequence[Task, Seq[Version]] map (v => SortedSet(v.flatten.toSeq: _*))
    versions map (_ filter isUpdate(current) filterNot lessStable(current))
  }

  private def lessStable(current: Version)(another: Version): Boolean = (current, another) match {
    case (ReleaseVersion(_), ReleaseVersion(_)) => false
    case (ReleaseVersion(_), _) => true
    case (SnapshotVersion(_, _, _), _) => false
    case (_, SnapshotVersion(_, _, _)) => true
    case (_, _) => false
  }

  private def isUpdate(current: Version) = current < _

  private val withEmpty: PartialFunction[Throwable, Seq[Version]] = {
    case _ => Seq.empty
  }

}
