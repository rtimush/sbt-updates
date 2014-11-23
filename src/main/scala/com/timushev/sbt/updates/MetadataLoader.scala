package com.timushev.sbt.updates

import java.net.URL

import com.timushev.sbt.updates.versions.Version
import sbt.{MavenRepository, ModuleID, Resolver}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

object MetadataLoaderFactory {
  val loader: PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo, download)
  }

  private val cache = mutable.Map[String, Future[Elem]]()

  def download(url: String) = synchronized {
    cache.getOrElseUpdate(url, Future(XML.load(new URL(url))))
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Future[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository, download: String => Future[xml.Elem]) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    download(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root.stripSuffix("/"))(_ + '/' + _)

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
