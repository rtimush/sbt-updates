package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.versions.Version
import sbt._

import java.io.FileNotFoundException
import scala.concurrent.Future

trait MetadataLoader {
  def repository: String

  /** Fails with FileNotFoundException when the repository doesn't have the module, and with another exception when the
    * repository could not be asked.
    */
  def getVersions(module: ModuleID): Future[Seq[Version]]
}

object MetadataLoader {
  private[metadata] def fromDocuments(urls: Seq[String])(fetch: String => Future[Seq[Version]]): Future[Seq[Version]] =
    Future
      .sequence(urls.map(url => fetch(url).map(Option(_)).recover { case _: FileNotFoundException => None }))
      .map { documents =>
        if (documents.nonEmpty && documents.forall(_.isEmpty)) throw new FileNotFoundException(urls.mkString(", "))
        documents.flatten.flatten
      }
}
