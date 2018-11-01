package com.timushev.sbt.updates

import org.scalatest.{FreeSpec, Matchers}

class ReporterSpec extends FreeSpec with Matchers {

  "A Reporter object" - {

    "with one dependency" - {
      import sbt._
      val dependencies = Seq[ModuleID]("test" % "test" % "1.0.0")

      "should return same dependencies when there are no dependenciesOverrides" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq.empty
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
      }

      "should return overridden dependencies when there are dependenciesOverrides" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq[ModuleID]("test" % "test" % "2.0.0")
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependenciesOverrides
      }

      "should NOT override dependencies when there are dependenciesOverrides with different coordinates" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq[ModuleID]("test" % "different" % "2.0.0")
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
      }
    }

    "with many dependencies" - {
      import sbt._
      val dependencies = Seq[ModuleID](
        "test" % "one" % "1.0.0",
        "test" % "two" % "2.0.0",
        "test" % "three" % "3.0.0",
        "test" % "four" % "4.0.0",
        "test" % "five" % "5.0.0"
      )

      "should return same dependencies when there are no dependenciesOverrides" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq.empty
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual dependencies
      }

      "should return correct dependencies when one of dependencies is overridden" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq[ModuleID]("test" % "three" % "4.0.0")
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual Seq(
          "test" % "one" % "1.0.0",
          "test" % "two" % "2.0.0",
          "test" % "three" % "4.0.0",
          "test" % "four" % "4.0.0",
          "test" % "five" % "5.0.0"
        )
      }

      "should return correct dependencies when some of dependencies are overridden" in {
        val dependenciesOverrides: Seq[sbt.ModuleID] = Seq[ModuleID](
          "test" % "three" % "4.0.0",
          "test" % "five" % "6.0.0"
        )
        Reporter.overrideDependencies(dependencies, dependenciesOverrides) shouldEqual Seq(
          "test" % "one" % "1.0.0",
          "test" % "two" % "2.0.0",
          "test" % "three" % "4.0.0",
          "test" % "four" % "4.0.0",
          "test" % "five" % "6.0.0"
        )
      }
    }
  }
}
