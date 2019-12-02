package com.timushev.sbt.updates.versions

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VersionSpec extends AnyFreeSpec with Matchers {
  "Versions" - {
    "should be classified correctly" in {
      List("1.0.0", "1.0.0.Final", "1.0.0-FINAL", "1.0.0.RELEASE").foreach { rel =>
        Version(rel) match {
          case ReleaseVersion(r) => r should equal(1 :: 0 :: 0 :: Nil)
          case _                 => fail(s"$rel is not a release version")
        }
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

      Version("2.0.2+9-4e5b95f4-SNAPSHOT") match { // Sbt-Dynver style snapshot version
        case SnapshotVersion(r, p, b) =>
          r should equal(2 :: 0 :: 2 :: Nil)
        case _ => fail("not a snaphshot version")
      }
    }
    "should be ordered according to the semantic versioning spec" in {
      val v = List(
        "invalid",
        "1.0.0-20131213005945",
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
        "1.33.7+build.11.e0f985a",
        "2.0.M5b",
        "2.0.M6-SNAP9",
        "2.0.M6-SNAP23",
        "2.0.M6-SNAP23a"
      )
      checkPairwise(v)
    }
    "snapshot versions are correctly ordered" in {
      val versions = List(
        "3.0.8",
        "3.1.0-SNAP6",
        "3.1.0-SNAP13",
        "3.1.0-alpha",
        "3.1.0-M2",
        "3.1.0-RC",
        "3.1.0-RC1",
        "3.1.0-RC2",
        "3.1.0",
        "3.2.0-SNAPSHOT1"
      )
      checkPairwise(versions)
    }
    "parser" - {
      "should parse versions like 1.0.M3" in {
        VersionParser.parse("1.0.M3") match {
          case VersionParser.Success((1 :: 0 :: Nil, "M3" :: Nil, Nil), _) =>
          case other                                                       => fail(other.toString)
        }
      }
      "should parse versions like 1.0.+" in {
        VersionParser.parse("1.0.+") match {
          case VersionParser.Success((1 :: 0 :: Long.MaxValue :: Nil, Nil, Nil), _) =>
          case other                                                                => fail(other.toString)
        }
      }

      "should reject versions like 1.+.+, 1.+.0, +.0.0" in {
        VersionParser.parse("1.+.+") match {
          case VersionParser.Failure(_, _) =>
          case other                       => fail(other.toString)
        }
        VersionParser.parse("1.+.0") match {
          case VersionParser.Failure(_, _) =>
          case other                       => fail(other.toString)
        }
        VersionParser.parse("+.0.0") match {
          case VersionParser.Failure(_, _) =>
          case other                       => fail(other.toString)
        }
      }
      "should parse versions like 1.0.3m" in {
        VersionParser.parse("1.0.3m") match {
          case VersionParser.Success((1 :: 0 :: Nil, "3m" :: Nil, Nil), _) =>
          case other                                                       => fail(other.toString)
        }
        VersionParser.parse("1.0.3m.4") match {
          case VersionParser.Success((1 :: 0 :: Nil, "3m" :: "4" :: Nil, Nil), _) =>
          case other                                                              => fail(other.toString)
        }
      }
      "should parse versions like 9.1-901-1.jdbc4" in {
        VersionParser.parse("9.1-901-1.jdbc4") match {
          case VersionParser.Success((9 :: 1 :: Nil, "901" :: "1" :: "jdbc4" :: Nil, Nil), _) =>
          case other                                                                          => fail(other.toString)
        }
      }
    }
  }

  def checkPairwise(versions: List[String]): Unit = {
    val pairs = versions.map(Version.apply).tails.flatMap {
      case h :: t => t.map((h, _))
      case Nil    => List.empty
    }
    pairs.foreach {
      case (a, b) =>
        a should be < b
        b should be > a
    }
  }
}
