package com.timushev.sbt.updates.versions

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec

class VersionSpec extends FreeSpec with ShouldMatchers {

  "Versions" - {
    "should be classified correctly" in {
      Version("1.0.0") match {
        case ReleaseVersion(r) => r should equal(1 :: 0 :: 0 :: Nil)
        case _ => fail("not a release version")
      }
      Version("1.0.0-alpha.1") match {
        case PreReleaseVersion(r, p) =>
          r should equal(1 :: 0 :: 0 :: Nil)
          p should equal("alpha" :: "1" :: Nil)
        case _ => fail("not a pre-release version")
      }
      Version("1.0.0-alpha.1+build.10") match {
        case PreReleaseBuildVersion(r, p, b) =>
          r should equal(1 :: 0 :: 0 :: Nil)
          p should equal("alpha" :: "1" :: Nil)
          b should equal("build" :: "10" :: Nil)
        case _ => fail("not a pre-release build version")
      }
      Version("1.0.0+build.10") match {
        case BuildVersion(r, b) =>
          r should equal(1 :: 0 :: 0 :: Nil)
          b should equal("build" :: "10" :: Nil)
        case _ => fail("not a build version")
      }
    }
    "should be ordered according to the semantic versioning spec" in {
      val v = List(
        "invalid",
        "1.0.0-alpha",
        "1.0.0-alpha.1",
        "1.0.0-beta.2",
        "1.0.0-beta.11",
        "1.0.0-rc.1",
        "1.0.0-rc.1+build.1",
        "1.0.0",
        "1.0.0+0.3.7",
        "1.33.7+build",
        "1.33.7+build.2.b8f12d7",
        "1.33.7+build.11.e0f985a"
      ).map(Version.apply)
      val pairs = v.tails.flatMap {
        case h :: t => t.map((h, _))
        case Nil => List.empty
      }
      pairs.foreach {
        case (a, b) =>
          a should be < (b)
          b should be > (a)
      }
    }
    "should handle versions like 1.0.M3 correctly" in {
      Version("1.0.M3") match {
        case PreReleaseVersion(1 :: 0 :: Nil, "M3" :: Nil) =>
        case _ => fail()
      }
    }
    "should handle versions like 1.0.3m correctly" in {
      Version("1.0.3m") match {
        case PreReleaseVersion(1 :: 0 :: Nil, "3m" :: Nil) =>
        case _ => fail()
      }
      Version("1.0.3m.4") match {
        case PreReleaseVersion(1 :: 0 :: Nil, "3m" :: "4" :: Nil) =>
        case _ => fail()
      }
    }
  }

}
