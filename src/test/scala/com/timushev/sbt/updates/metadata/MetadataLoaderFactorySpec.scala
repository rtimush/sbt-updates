package com.timushev.sbt.updates.metadata

import java.net.URL

import sbt.{ConsoleLogger, Resolver}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MetadataLoaderFactorySpec extends AnyFreeSpec with Matchers {
  val logger = ConsoleLogger()

  "A MetadataLoader factory" - {
    "should return a CachingMetadataLoader for maven repositories" in {
      MetadataLoaderFactory.loader(logger, Nil).apply(Resolver.jcenterRepo) should have(
        Symbol("class")(classOf[CachingMetadataLoader])
      )
    }
    "should return a CachingMetadataLoader for ivy repositories" in {
      MetadataLoaderFactory.loader(logger, Nil).apply(Resolver.sbtPluginRepo("releases")) should have(
        Symbol("class")(classOf[CachingMetadataLoader])
      )
    }
    "should ignore unknown protocols" in {
      val ftpResolver = Resolver.url("ftp", new URL("ftp://localhost/"))
      MetadataLoaderFactory.loader(logger, Nil).isDefinedAt(ftpResolver) shouldBe false
    }
  }
}
