package com.timushev.sbt.updates

import org.scalatest.{Matchers, FreeSpec}
import scala.concurrent.Await
import scala.concurrent.duration._

class MetadataLoaderSpec extends FreeSpec with Matchers {
  "The metadata loader factory" - {
    "for a broken url" - {
      val logger = new MockLogger()
      "should return a none" in {
        Await.result(MetadataLoaderFactory.download(logger)("s3://somebucket"), 1.minute) should be('empty)
      }
    }
  }
}
