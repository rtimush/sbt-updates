package com.timushev.sbt.updates.metadata

import java.net.URL

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.versions.Version
import sbt.{MavenRepository, ModuleID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.XML

class MavenMetadataLoader(repo: MavenRepository, downloader: Downloader) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    downloadXML(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID): String =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID): String =
    (module.organization.split('.') :+ module.name).foldLeft(repo.root.stripSuffix("/"))(_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    (metadata \ "versioning" \ "versions" \ "version").map(_.text).map(Version.apply)

  private def downloadXML(url: String) = Future {
    XML.load(downloader.startDownload(new URL(url)))
  }

}
