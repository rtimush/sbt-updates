package com.timushev.sbt.updates.metadata

import java.net.URL

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.matching.Regex

object MetadataLoaderFactory {
  val KnownProtocol: Regex = "(?i)^https?$".r
  val KnownProtocolUrl: Regex = "(?i)^https?://".r

  def loader(logger: Logger, credentials: Seq[Credentials]): PartialFunction[Resolver, MetadataLoader] = {
    Function.unlift { resolver =>
      loaderCache.synchronized {
        loaderCache.getOrElseUpdate(resolver, newLoader(logger, credentials, resolver).map(cached))
      }
    }
  }

  private def newLoader(logger: Logger, credentials: Seq[Credentials], resolver: Resolver) = resolver match {
    case repo: MavenRepository =>
      val downloader = new Downloader(credentials, logger)
      val url = new URL(repo.root)
      url.getProtocol match {
        case KnownProtocol() =>
          val resolver = Resolver.url(repo.name, url)(Resolver.mavenStylePatterns)
          val mavenLoader = new MavenMetadataLoader(resolver, downloader)
          val ivyLoader = new IvyMetadataLoader(resolver, downloader)
          Some(new CompoundMetadataLoader(Seq(mavenLoader, ivyLoader)))
        case _ => None
      }
    case repo: URLRepository =>
      if (repo.patterns.artifactPatterns.forall(KnownProtocolUrl.findFirstIn(_).nonEmpty)) {
        val downloader = new Downloader(credentials, logger)
        Some(new IvyMetadataLoader(repo, downloader))
      } else {
        None
      }
    case _ =>
      None
  }

  private val loaderCache = mutable.Map[Resolver, Option[MetadataLoader]]()
  private val cache = mutable.Map[(MetadataLoader, ModuleID), Future[Seq[Version]]]()

  private def cached(loader: MetadataLoader) = new CachingMetadataLoader(loader, cache)
}
