package com.timushev.sbt.updates.metadata

import java.net.URI

import com.timushev.sbt.updates.LookupSession
import sbt.{ConsoleLogger, Resolver}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MetadataLoaderFactorySpec extends AnyFreeSpec with Matchers {
  val logger = ConsoleLogger()

  private def loader = MetadataLoaderFactory.loader(new LookupSession, logger, Nil)

  "A MetadataLoader factory" - {
    "should read Maven metadata and directory listings for maven repositories" in {
      loader.apply(Resolver.mavenCentral) should have(
        Symbol("class")(classOf[CompoundMetadataLoader])
      )
    }
    "should read directory listings for ivy repositories" in {
      loader.apply(Resolver.sbtPluginRepo("releases")) should have(
        Symbol("class")(classOf[IvyMetadataLoader])
      )
    }
    "should name loaders after their repository" in {
      loader.apply(Resolver.mavenCentral).repository shouldBe Resolver.mavenCentral.name
    }
    "should ignore unknown protocols" in {
      val ftpResolver = Resolver.url("ftp", new URI("ftp://localhost/").toURL)
      loader.isDefinedAt(ftpResolver) shouldBe false
    }
  }
}
