package com.timushev.sbt.updates.metadata

import java.net.URL

import org.scalatest.{FreeSpec, Matchers}
import sbt.{ConsoleLogger, Resolver}

class MetadataLoaderFactorySpec extends FreeSpec with Matchers {
  val logger = ConsoleLogger()

  "A MetadataLoader factory" - {
    "should return a CachingMetadataLoader for maven repositories" in {
      MetadataLoaderFactory.loader(logger, Nil).apply(Resolver.jcenterRepo) should have(
        'class (classOf[CachingMetadataLoader])
      )
    }
    "should return a CachingMetadataLoader for ivy repositories" in {
      MetadataLoaderFactory.loader(logger, Nil).apply(Resolver.sbtPluginRepo("releases")) should have(
        'class (classOf[CachingMetadataLoader])
      )
    }
    "should ignore unknown protocols" in {
      val ftpResolver = Resolver.url("ftp", new URL("ftp://localhost/"))
      MetadataLoaderFactory.loader(logger, Nil).isDefinedAt(ftpResolver) shouldBe false
    }
  }
}
