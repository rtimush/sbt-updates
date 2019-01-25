package com.timushev.sbt.updates.metadata

import java.net.URL
import java.util

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.versions.Version
import org.apache.ivy.core.IvyPatternHelper
import sbt.{ModuleID, URLRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.XML

class MavenMetadataLoader(repo: URLRepository, downloader: Downloader) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] = {
    Future
      .sequence(
        repo.patterns.artifactPatterns
          .flatMap(url(_, module))
          .map(download)
          .map(_.map(extractVersions)))
      .map(_.flatten)
  }

  private def url(pattern: String, module: ModuleID): Option[String] = {
    val tokens = new util.HashMap[String, String]()
    val org = module.organization.split("\\.").mkString("/")
    tokens.put(IvyPatternHelper.ORGANISATION_KEY, org)
    tokens.put(IvyPatternHelper.ORGANISATION_KEY2, org)
    tokens.put(IvyPatternHelper.MODULE_KEY, module.name)
    module.configurations.foreach(tokens.put(IvyPatternHelper.CONF_KEY, _))
    module.extraAttributes.foreach { case (k, v) => tokens.put(removeE(k), v) }
    val substituted = IvyPatternHelper.substituteTokens(pattern, tokens)
    if (IvyPatternHelper.getFirstToken(substituted) == IvyPatternHelper.REVISION_KEY) {
      Some(IvyPatternHelper.getTokenRoot(substituted)).map(_ + "maven-metadata.xml")
    } else {
      None
    }
  }

  def extractVersions(metadata: xml.Elem): Seq[Version] =
    (metadata \ "versioning" \ "versions" \ "version").map(_.text).map(Version.apply)

  private def download(url: String) = Future {
    XML.load(downloader.startDownload(new URL(url)))
  }

  private def removeE(s: String): String = {
    if (s.startsWith("e:")) s.substring(2) else s
  }

}
