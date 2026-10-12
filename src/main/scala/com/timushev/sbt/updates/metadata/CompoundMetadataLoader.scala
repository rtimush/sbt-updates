package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.versions.Version
import sbt.ModuleID

import java.io.FileNotFoundException
import scala.concurrent.Future
import scala.util.control.NonFatal

/** Adds the versions of a directory listing to those of the Maven metadata, which can be stale (e.g. it lists no 2.0.x
  * versions of sbt-scalafmt on Maven Central). Many repositories serve no listings, so only the metadata can fail a
  * lookup.
  */
class CompoundMetadataLoader(val repository: String, metadata: MetadataLoader, listing: MetadataLoader)
    extends MetadataLoader {
  override def getVersions(module: ModuleID): Future[Seq[Version]] = {
    val listed = listing.getVersions(module).map(Option(_)).recover { case NonFatal(_) => None }
    metadata
      .getVersions(module)
      .map(Option(_))
      .recover { case _: FileNotFoundException => None }
      .zipWith(listed) {
        case (None, None)     => throw new FileNotFoundException(s"$module in $repository")
        case (found, another) => found.getOrElse(Nil) ++ another.getOrElse(Nil)
      }
  }
}
