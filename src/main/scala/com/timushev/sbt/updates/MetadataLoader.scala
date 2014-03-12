package com.timushev.sbt.updates

import sbt.{Resolver, MavenRepository, ModuleID}
import versions.Version
import scala.xml.XML
import scalaz.concurrent._
import scalaz.Memo._
import java.net.URL

object MetadataLoaderFactory {
  val loader: PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo, download)
  }

  def download(url: String) = synchronized(doDownload(url))
  private val doDownload = immutableHashMapMemo((url: String) => Task(XML.load(new URL(url))))
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Task[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository, download: String => Task[xml.Elem]) extends MetadataLoader {

  def getVersions(module: ModuleID): Task[Seq[Version]] =
    download(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root.stripSuffix("/"))(_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
