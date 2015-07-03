package com.timushev.sbt.updates

import java.net.URL

import com.timushev.sbt.updates.versions.Version
import sbt.{Logger, MavenRepository, ModuleID, Resolver}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Try}
import scala.xml.{Elem, XML}

object MetadataLoaderFactory {
  def loader(logger: Logger): PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo, download(logger))
  }

  private val cache = mutable.Map[String, Future[Option[Elem]]]()

  def download(logger: Logger)(url: String) = synchronized {
    cache.getOrElseUpdate(url, Future {
      logger.debug(s"Downloading $url")
      Try {
        XML.load(new URL(url))
      }.recoverWith {
        case NonFatal(e) =>
          logger.warn(s"Could not download $url: ${e.getMessage}")
          Failure(e)
      }.toOption
    })
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Future[Seq[Version]]
}

class MavenMetadataLoader(repo: MavenRepository, download: String => Future[Option[xml.Elem]]) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] =
    download(metadataUrl(module)).map(extractVersions)

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) + "/maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft repo.root.stripSuffix("/"))(_ + '/' + _)

  def extractVersions(metadata: Option[xml.Elem]): Seq[Version] = metadata.toSeq.flatMap(
    _ \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply
  )
}
