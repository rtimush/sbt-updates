package com.timushev.sbt.updates.metadata

import java.net.URI
import com.timushev.sbt.updates.{Downloader, LookupSession}
import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import sbt._

import scala.util.matching.Regex

object MetadataLoaderFactory {
  val KnownProtocol: Regex    = "(?i)^https?$".r
  val KnownProtocolUrl: Regex = "(?i)^https?://".r

  def loader(
      session: LookupSession,
      logger: Logger,
      authentications: Seq[RepositoryAuthentication]
  ): PartialFunction[Resolver, MetadataLoader] =
    Function.unlift((resolver: Resolver) => newLoader(session, logger, authentications, resolver))

  private def newLoader(
      session: LookupSession,
      logger: Logger,
      authentications: Seq[RepositoryAuthentication],
      resolver: Resolver
  ): Option[MetadataLoader] =
    resolver match {
      case repo: MavenRepository =>
        val url = new URI(repo.root).toURL
        url.getProtocol match {
          case KnownProtocol() =>
            implicit val patterns: Patterns = Resolver.mavenStylePatterns
            val repository                  = Resolver.url(repo.name, url)
            val downloader                  = new Downloader(repo.name, authentications, logger, session)
            val metadata                    = new MavenMetadataLoader(repository, downloader, session)
            val listing                     = new IvyMetadataLoader(repository, downloader, session)
            Some(new CompoundMetadataLoader(repo.name, metadata, listing))
          case _ => None
        }
      case repo: URLRepository =>
        if (repo.patterns.artifactPatterns.forall(KnownProtocolUrl.findFirstIn(_).nonEmpty)) {
          val downloader = new Downloader(repo.name, authentications, logger, session)
          Some(new IvyMetadataLoader(repo, downloader, session))
        } else
          None
      case _ =>
        None
    }
}
