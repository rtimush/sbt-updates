package com.timushev.sbt.updates

import sbt.{globFilter => _, _}
import com.timushev.sbt.updates.versions.Version
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.SortedSet

class ReporterSpec extends AnyFreeSpec with Matchers {
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
  }
  "Reporter" - {
    "on CSV" - {
      "should return a valid CSV file containing all dependencies upgrades available" in {
        val input = Map(
          "test" % "three" % "4.0.0" -> SortedSet(Version("4.0.1"), Version("4.2.0"), Version("5.4.8")),
          "test" % "five"  % "6.0.0" -> SortedSet(Version("6.0.7"), Version("6.3.0"))
        )
        val expectation =
          """test:five;6.0.0;6.0.7;6.3.0
            |test:three;4.0.0;4.0.1;4.2.0;5.4.8""".stripMargin
        Reporter.dependencyUpdatesCsvReport(input) shouldEqual expectation
      }
    }
  }
}
