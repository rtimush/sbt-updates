package com.timushev.sbt.updates.metadata

import java.io.FileNotFoundException
import java.net.URL
import java.util

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.metadata.extractor.HtmlVersionExtractor
import com.timushev.sbt.updates.versions.Version
import org.apache.ivy.core.IvyPatternHelper
import sbt.{IO, ModuleID, URLRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.Exception.catching

object IvyMetadataLoader {
  val VersionExtractor = new HtmlVersionExtractor
}

class IvyMetadataLoader(repo: URLRepository, downloader: Downloader) extends MetadataLoader {

  def getVersions(module: ModuleID): Future[Seq[Version]] = {
    val prefixes = repo.patterns.artifactPatterns.flatMap(getRevisionPrefix(_, module))
    Future.sequence(prefixes.map(download)).map(_.flatten.flatMap(extractVersions))
  }

  private def getRevisionPrefix(pattern: String, module: ModuleID): Option[String] = {
    val tokens = new util.HashMap[String, String]()
    val organization =
      if (repo.patterns.isMavenCompatible) module.organization.replace('.', '/')
      else module.organization
    tokens.put(IvyPatternHelper.ORGANISATION_KEY, organization)
    tokens.put(IvyPatternHelper.ORGANISATION_KEY2, organization)
    tokens.put(IvyPatternHelper.MODULE_KEY, module.name)
    module.configurations.foreach(tokens.put(IvyPatternHelper.CONF_KEY, _))
    module.extraAttributes.foreach { case (k, v) => tokens.put(removeE(k), v) }
    val substituted = IvyPatternHelper.substituteTokens(pattern, tokens)
    if (IvyPatternHelper.getFirstToken(substituted) == IvyPatternHelper.REVISION_KEY) {
      Some(IvyPatternHelper.getTokenRoot(substituted))
    } else {
      None
    }
  }

  private def download(url: String): Future[Option[String]] = {
    Future {
      catching(classOf[FileNotFoundException]).opt {
        val is = downloader.startDownload(new URL(url))
        try IO.readStream(is)
        finally is.close()
      }
    }
  }

  private def extractVersions(data: String): Seq[Version] = {
    IvyMetadataLoader.VersionExtractor.applyOrElse(data, (_: String) => Nil)
  }

  private def removeE(s: String): String = {
    if (s.startsWith("e:")) s.substring(2) else s
  }

}
