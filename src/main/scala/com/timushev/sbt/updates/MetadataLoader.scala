package com.timushev.sbt.updates

import java.net.URL
import javax.xml.bind.DatatypeConverter

import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

object MetadataLoaderFactory {
  def loader(logger: Logger, credentials: Seq[Credentials]): PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository =>
      val repoCredentials = Credentials.forHost(credentials, new URL(repo.root).getHost)
      new MavenMetadataLoader(repo, download(logger, repoCredentials))
  }

  private val cache = mutable.Map[String, Future[Elem]]()

  def download(logger: Logger, credentials: Option[DirectCredentials])(url: String) = synchronized {
    cache.getOrElseUpdate(url, Future {
      credentials match {
        case Some(c) =>
          val auth = DatatypeConverter.printBase64Binary(s"${c.userName}:${c.passwd}".getBytes)
          val connection = new URL(url).openConnection()
          connection.setRequestProperty("Authorization", s"Basic $auth")
          logger.debug(s"Downloading $url as ${c.userName}")
          XML.load(connection.getInputStream)
        case None =>
          logger.debug(s"Downloading $url")
          XML.load(new URL(url))
      }
    })
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
