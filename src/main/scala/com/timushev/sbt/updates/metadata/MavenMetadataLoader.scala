package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.versions.Version
import com.timushev.sbt.updates.{Downloader, LookupSession}
import sbt.{ModuleID, URLRepository}

import java.net.URI
import scala.concurrent.Future
import scala.xml.XML

class MavenMetadataLoader(repo: URLRepository, downloader: Downloader, session: LookupSession) extends MetadataLoader {
  def repository: String = repo.name

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    MetadataLoader.fromDocuments(repo.patterns.artifactPatterns.flatMap(url(_, module)))(download)

  private def url(pattern: String, module: ModuleID): Option[String] =
    RepositoryPattern
      .revisionPrefix(pattern, module, module.organization.split("\\.").mkString("/"))
      .map(_ + "maven-metadata.xml")

  private def download(url: String): Future[Seq[Version]] = {
    val location = new URI(url).toURL
    session.document("maven-metadata", location, downloader.authentication(location)) {
      Future {
        val in = downloader.startDownload(location)
        try extractVersions(XML.load(in))
        finally in.close()
      }
    }
  }

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    (metadata \ "versioning" \ "versions" \ "version").map(_.text).map(Version.apply)
}
