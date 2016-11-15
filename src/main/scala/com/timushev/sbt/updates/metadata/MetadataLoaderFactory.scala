package com.timushev.sbt.updates.metadata

import java.net.URL

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.mutable
import scala.concurrent.Future

object MetadataLoaderFactory {

  val KnownProtocol = "(?i)^https?$".r

  def loader(logger: Logger, credentials: Seq[Credentials]): PartialFunction[Resolver, MetadataLoader] = {
    Function.unlift { resolver =>
      loaderCache.getOrElseUpdate(resolver, newLoader(logger, credentials, resolver).map(cached))
    }
  }

  private def newLoader(logger: Logger, credentials: Seq[Credentials], resolver: Resolver) = resolver match {
    case repo: MavenRepository =>
      val downloader = new Downloader(credentials, logger)
      val url = new URL(repo.root)
      url.getProtocol match {
        case KnownProtocol() => Some(new MavenMetadataLoader(repo, downloader))
        case _ => None
      }
    case repo: URLRepository =>
      val downloader = new Downloader(credentials, logger)
      Some(new IvyMetadataLoader(repo, downloader))
    case repo =>
      None
  }

  private val loaderCache = mutable.Map[Resolver, Option[MetadataLoader]]()
  private val cache = mutable.Map[(MetadataLoader, ModuleID), Future[Seq[Version]]]()

  private def cached(loader: MetadataLoader) = new CachingMetadataLoader(loader, cache)

}
