package com.timushev.sbt.updates

import java.net.URL

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

object MetadataLoaderFactory {

  val KnownProtocol = "(?i)^https?$".r

  def loader(logger: Logger, credentials: Seq[Credentials]): PartialFunction[Resolver, MetadataLoader] = Function.unlift {
    case repo: MavenRepository =>
      val downloader = new Downloader(credentials, logger)
      val url = new URL(repo.root)
      url.getProtocol match {
        case KnownProtocol() => Some(new MavenMetadataLoader(repo, downloadXML(downloader)))
        case _ => None
      }
    case _ =>
      None
  }

  private val cache = mutable.Map[String, Future[Elem]]()

  def downloadXML(downloader: Downloader)(url: String) = synchronized {
    cache.getOrElseUpdate(url, Future {
      val is = downloader.startDownload(new URL(url))
      XML.load(is)
    })
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Future[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository, downloadXML: String => Future[xml.Elem]) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    downloadXML(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root.stripSuffix("/")) (_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
