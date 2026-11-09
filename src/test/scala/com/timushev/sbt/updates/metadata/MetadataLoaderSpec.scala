package com.timushev.sbt.updates.metadata

import java.io.{ByteArrayInputStream, FileNotFoundException, IOException, InputStream}
import java.net.{URI, URL}
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicInteger

import com.timushev.sbt.updates.{Downloader, HttpStatusException, LookupSession}
import com.timushev.sbt.updates.versions.Version
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.xml.sax.SAXParseException
import sbt.{ConsoleLogger, ModuleID, Patterns, Resolver, URLRepository}

import scala.concurrent.Await
import scala.concurrent.duration._

class MetadataLoaderSpec extends AnyFreeSpec with Matchers {
  private val module      = ModuleID("org.example", "sample", "1.0")
  private val root        = new URI("https://repo.example/").toURL
  private val metadataUrl = "https://repo.example/org/example/sample/maven-metadata.xml"
  private val listingUrl  = "https://repo.example/org/example/sample/"
  private val metadata    =
    "<metadata><versioning><versions><version>1.0</version><version>2.0</version></versions></versioning></metadata>"

  // Serves the given documents, fails the given URLs and answers 404 for anything else.
  private class FakeDownloader(
      documents: Map[String, String],
      failing: Map[String, IOException] = Map.empty,
      val lookupSession: LookupSession = new LookupSession
  ) extends Downloader("test", Nil, ConsoleLogger(), lookupSession) {
    val downloads = new AtomicInteger

    override def startDownload(url: URL): InputStream = {
      downloads.incrementAndGet()
      failing.get(url.toString).foreach(e => throw e)
      documents.get(url.toString) match {
        case Some(document) => new ByteArrayInputStream(document.getBytes(UTF_8))
        case None           => throw new FileNotFoundException(url.toString)
      }
    }
  }

  private def repository(patterns: Patterns): URLRepository = {
    implicit val basePatterns: Patterns = patterns
    Resolver.url("test", root)
  }

  private def maven(downloader: FakeDownloader) =
    new MavenMetadataLoader(repository(Resolver.mavenStylePatterns), downloader, downloader.lookupSession)

  private def mavenWithListing(downloader: FakeDownloader) = {
    val repo = repository(Resolver.mavenStylePatterns)
    new CompoundMetadataLoader(
      "test",
      new MavenMetadataLoader(repo, downloader, downloader.lookupSession),
      new IvyMetadataLoader(repo, downloader, downloader.lookupSession)
    )
  }

  private def forbidden(url: String) = Map(url -> new HttpStatusException(new URI(url).toURL, 403))

  private def versions(loader: MetadataLoader, module: ModuleID = module): Seq[Version] =
    Await.result(loader.getVersions(module), 5.seconds)

  "Metadata loaders" - {
    "fetch Maven metadata and return its available versions" in {
      versions(maven(new FakeDownloader(Map(metadataUrl -> metadata)))) shouldBe Seq(Version("1.0"), Version("2.0"))
    }
    "fetch Ivy directory listings with optional cross-version attributes" in {
      val repo = repository(
        Patterns(
          "[organisation]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[artifact].[ext]"
        ).withIsMavenCompatible(false)
      )
      val downloader = new FakeDownloader(
        Map(
          "https://repo.example/org.example/sample/scala_3/" -> "<html><a href=\"1.0/\">1.0/</a><a href=\"2.0/\">2.0/</a></html>"
        )
      )
      versions(
        new IvyMetadataLoader(repo, downloader, downloader.lookupSession),
        module.extra("scalaVersion" -> "3")
      ) shouldBe
        Seq(Version("1.0"), Version("2.0"))
    }
    "skip patterns with unresolved attributes before the revision without downloading" in {
      val repo       = repository(Patterns("[unknown]/[revision]/[artifact]"))
      val downloader = new FakeDownloader(Map.empty)
      val loaders    = Seq(
        new MavenMetadataLoader(repo, downloader, downloader.lookupSession),
        new IvyMetadataLoader(repo, downloader, downloader.lookupSession)
      )
      for (loader <- loaders) versions(loader) shouldBe empty
      downloader.downloads.get shouldBe 0
    }
    "reject Maven metadata that declares a DOCTYPE" in {
      val downloader = new FakeDownloader(
        Map(
          metadataUrl -> ("<!DOCTYPE metadata [<!ENTITY xxe SYSTEM \"file:///etc/hostname\">]>" +
            "<metadata><versioning><versions><version>&xxe;</version></versions></versioning></metadata>")
        )
      )
      a[SAXParseException] should be thrownBy versions(maven(downloader))
    }
    "report a module the repository doesn't have as not found" in {
      a[FileNotFoundException] should be thrownBy versions(maven(new FakeDownloader(Map.empty)))
    }
    "add the versions of the directory listing to stale Maven metadata" in {
      val listing = "<html><a href=\"3.0/\">3.0/</a></html>"
      versions(mavenWithListing(new FakeDownloader(Map(metadataUrl -> metadata, listingUrl -> listing)))) shouldBe
        Seq(Version("1.0"), Version("2.0"), Version("3.0"))
      versions(mavenWithListing(new FakeDownloader(Map(listingUrl -> listing)))) shouldBe Seq(Version("3.0"))
    }
    "ignore repositories that serve no directory listings" in {
      versions(mavenWithListing(new FakeDownloader(Map(metadataUrl -> metadata), forbidden(listingUrl)))) shouldBe
        Seq(Version("1.0"), Version("2.0"))
      a[FileNotFoundException] should be thrownBy versions(
        mavenWithListing(new FakeDownloader(Map.empty, forbidden(listingUrl)))
      )
    }
    "report Maven metadata that could not be fetched" in {
      a[HttpStatusException] should be thrownBy versions(
        mavenWithListing(new FakeDownloader(Map(listingUrl -> "<html></html>"), forbidden(metadataUrl)))
      )
    }
    "fetch a document once per session" in {
      val downloader = new FakeDownloader(Map(metadataUrl -> metadata))
      versions(maven(downloader))
      versions(maven(downloader), module.withRevision("2.0"))
      downloader.downloads.get shouldBe 1
      val nextSession = new FakeDownloader(Map(metadataUrl -> metadata))
      versions(maven(nextSession))
      nextSession.downloads.get shouldBe 1
    }
  }
}
