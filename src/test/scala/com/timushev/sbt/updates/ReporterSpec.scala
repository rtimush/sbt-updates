package com.timushev.sbt.updates

import sbt.{globFilter => _, _}
import com.timushev.sbt.updates.UpdatesFinder.LookupFailure
import com.timushev.sbt.updates.versions.Version
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.IOException
import scala.collection.immutable.SortedSet
import scala.collection.mutable

class ReporterSpec extends AnyFreeSpec with Matchers {
  private class RecordingLogger extends Logger {
    val messages = mutable.Buffer.empty[(Level.Value, String)]

    def warnings: Seq[String] = messages.collect { case (Level.Warn, message) => message }.toSeq
    def debugs: Seq[String]   = messages.collect { case (Level.Debug, message) => message }.toSeq

    override def trace(t: => Throwable): Unit                      = ()
    override def success(message: => String): Unit                 = ()
    override def log(level: Level.Value, message: => String): Unit = messages += ((level, message))
  }

  "A Reporter object" - {
    "overriding dependencies" - {
      "with one dependency" - {
        val dependencies = Seq[ModuleID]("test" % "test" % "1.0.0")
        "should return same dependencies when there are no dependenciesOverrides" in {
          val dependenciesOverrides = Seq.empty[ModuleID]
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
        }

        "should return overridden dependencies when there are dependenciesOverrides" in {
          val dependenciesOverrides = Seq[ModuleID]("test" % "test" % "2.0.0")
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependenciesOverrides
        }

        "should not override dependencies when there are dependenciesOverrides with different coordinates" in {
          val dependenciesOverrides = Seq[ModuleID]("test" % "different" % "2.0.0")
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
        }
      }

      "with many dependencies" - {
        val dependencies = Seq[ModuleID](
          "test" % "one"   % "1.0.0",
          "test" % "two"   % "2.0.0",
          "test" % "three" % "3.0.0",
          "test" % "four"  % "4.0.0",
          "test" % "five"  % "5.0.0"
        )

        "should return same dependencies when there are no dependenciesOverrides" in {
          val dependenciesOverrides = Seq.empty[ModuleID]
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
        }

        "should return correct dependencies when one of dependencies is overridden" in {
          val dependenciesOverrides = Seq[ModuleID]("test" % "three" % "4.0.0")
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual Seq(
            "test" % "one"   % "1.0.0",
            "test" % "two"   % "2.0.0",
            "test" % "three" % "4.0.0",
            "test" % "four"  % "4.0.0",
            "test" % "five"  % "5.0.0"
          )
        }

        "should return correct dependencies when some of dependencies are overridden" in {
          val dependenciesOverrides = Seq[ModuleID](
            "test" % "three" % "4.0.0",
            "test" % "five"  % "6.0.0"
          )
          Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual Seq(
            "test" % "one"   % "1.0.0",
            "test" % "two"   % "2.0.0",
            "test" % "three" % "4.0.0",
            "test" % "four"  % "4.0.0",
            "test" % "five"  % "6.0.0"
          )
        }
      }

      "with cross-versioning" - {
        "should return overridden dependencies when overriding with an explicit cross-version" in {
          val dependencies          = Seq[ModuleID]("test" %% "test" % "1.0.0")
          val dependenciesOverrides = Seq[ModuleID]("test" % "test_2.12" % "2.0.0")
          Reporter
            .finalDependencies("2.12.1", dependencies, dependenciesOverrides)
            .map(_.revision) shouldEqual Seq("2.0.0")
        }

        "should not override dependencies when overriding with a different explicit cross-version" in {
          val dependencies          = Seq[ModuleID]("test" %% "test" % "1.0.0")
          val dependenciesOverrides = Seq[ModuleID]("test" % "test_2.11" % "2.0.0")
          Reporter
            .finalDependencies("2.12.1", dependencies, dependenciesOverrides)
            .map(_.revision) shouldEqual Seq("1.0.0")
        }
      }
    }

    "classifying updates" - {
      "should treat a missing minor or patch number as zero" in {
        val current = Version("2")
        val updates = SortedSet(Version("2.0.5"), Version("2.1"), Version("3"))
        Reporter.patchUpdate(current, updates) shouldBe Some(Version("2.0.5"))
        Reporter.minorUpdate(current, updates) shouldBe Some(Version("2.1"))
        Reporter.majorUpdate(current, updates) shouldBe Some(Version("3"))
      }
    }

    "finding the dependencies of a build" - {
      "should keep dependencies declared in build sources under project/src" in
        IO.withTemporaryDirectory { root =>
          IO.touch(root / "project" / "Flat.scala")
          IO.touch(root / "project" / "src" / "main" / "scala" / "deps" / "Nested.scala")
          val flat      = "org.example" % "flat"   % "1.0.0"
          val nested    = "org.example" % "nested" % "1.0.0"
          val plugin    = "org.example" % "plugin" % "1.0.0"
          val positions = Map[ModuleID, Set[SourcePosition]](
            flat   -> Set(sbt.internal.util.LinePosition("(Flat.deps) Flat.scala", 3)),
            nested -> Set(sbt.internal.util.LinePosition("(deps.Nested.deps) Nested.scala", 3)),
            plugin -> Set(sbt.internal.util.LinePosition("(some.Plugin.projectSettings) Plugin.scala", 3))
          )
          Reporter.excludeDependenciesFromPlugins(Seq(flat, nested, plugin), positions, root) shouldBe Seq(flat, nested)
        }
    }
  }
  "Reporter" - {
    "on CSV" - {
      "should return a valid CSV file containing all dependencies upgrades available" in {
        val input = Map(
          "test" % "three" % "4.0.0" -> SortedSet(Version("4.0.1"), Version("4.2.0"), Version("5.4.8")),
          "test" % "five"  % "6.0.0" -> SortedSet(Version("6.0.7"), Version("6.3.0"))
        )
        val expectation =
          """test:five;6.0.0;6.0.7;6.3.0;
            |test:three;4.0.0;4.0.1;4.2.0;5.4.8""".stripMargin
        Reporter.dependencyUpdatesCsvReport(input) shouldEqual expectation
      }
      "should keep empty cells so that each column is always the same kind of update" in {
        val input = Map(
          "test" % "minor" % "1.0.0" -> SortedSet(Version("1.2.0")),
          "test" % "patch" % "1.0.0" -> SortedSet(Version("1.0.1"))
        )
        Reporter.dependencyUpdatesCsvReport(input) shouldEqual "test:minor;1.0.0;;1.2.0;\ntest:patch;1.0.0;1.0.1;;"
      }
    }
    "on lookup failures" - {
      "should warn once per repository" in {
        val log    = new RecordingLogger
        val module = "org.example" % "lib" % "1.0.0"
        Reporter.reportFailures(
          "org.example" % "project" % "0.1.0",
          Seq(
            LookupFailure("private", module, new IOException("HTTP 401 for https://repo.example/lib")),
            LookupFailure(
              "private",
              module.withName("other"),
              new IOException("HTTP 401 for https://repo.example/other")
            ),
            LookupFailure("mirror", module, new IOException("HTTP 503 for https://mirror.example/lib"))
          ),
          log
        )
        log.warnings shouldBe Seq(
          "project: could not check 1 module in repository mirror (HTTP 503 for https://mirror.example/lib); updates may be missing",
          "project: could not check 2 modules in repository private (HTTP 401 for https://repo.example/lib); updates may be missing"
        )
      }
    }
    "on a project opted out of cross-building" - {
      "should still check its dependencies for its own Scala version" in {
        Reporter.scalaVersionsToCheck("3.3.4", Nil) shouldBe Seq("3.3.4")
        Reporter.scalaVersionsToCheck("3.3.4", Seq("2.13.15", "3.3.4")) shouldBe Seq("2.13.15", "3.3.4")
      }
    }
    "on a Coursier configuration it cannot read" - {
      "should continue without its credentials" in {
        val log = new RecordingLogger
        Reporter.coursierAuthentications(new Object, log) shouldBe empty
        log.debugs should have size 1
      }
    }
  }
}
