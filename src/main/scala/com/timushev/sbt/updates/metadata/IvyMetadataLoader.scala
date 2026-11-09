package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.metadata.extractor.HtmlVersionExtractor
import com.timushev.sbt.updates.versions.Version
import com.timushev.sbt.updates.{Downloader, LookupSession}
import sbt.{IO, ModuleID, URLRepository}

import java.net.URI
import scala.concurrent.Future

object IvyMetadataLoader {
  val VersionExtractor = new HtmlVersionExtractor
}

class IvyMetadataLoader(repo: URLRepository, downloader: Downloader, session: LookupSession) extends MetadataLoader {
  def repository: String = repo.name

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    MetadataLoader.fromDocuments(repo.patterns.artifactPatterns.flatMap(getRevisionPrefix(_, module)))(download)

  private def getRevisionPrefix(pattern: String, module: ModuleID): Option[String] =
    RepositoryPattern.revisionPrefix(
      pattern,
      module,
      if (repo.patterns.isMavenCompatible) module.organization.replace('.', '/') else module.organization
    )

  private def download(url: String): Future[Seq[Version]] = {
    val location = new URI(url).toURL
    session.document("directory-listing", location, downloader.authentication(location)) {
      Future {
        val is   = downloader.startDownload(location)
        val data =
          try IO.readStream(is)
          finally is.close()
        extractVersions(data)
      }
    }
  }

  private def extractVersions(data: String): Seq[Version] =
    IvyMetadataLoader.VersionExtractor.applyOrElse(data, (_: String) => Nil)

}
