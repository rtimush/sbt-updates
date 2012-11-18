package com.timushev.sbt.updates

import org.scalatest.FreeSpec
import sbt._
import org.scalatest.matchers.ShouldMatchers

class UpdatesFinderSpec extends FreeSpec with ShouldMatchers {

  def updates(current: String, available: Seq[String]): Set[String] =
    UpdatesFinder.findUpdates(Seq(new FixedMetadataLoader(available)))(ModuleID("a", "b", current))
      .apply()
      .map(_.toString())

  val available = Seq(
    "0.9.9-SNAPSHOT", "0.9.9",
    "1.0.0-SNAPSHOT", "1.0.0",
    "1.0.1-SNAPSHOT", "1.0.1"
  )

  "An updates finder" - {
    "for snapshot artifacts" - {
      val u = updates("1.0.0-SNAPSHOT", available)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should show stable updates" in {
        u should (contain("1.0.0") and contain("1.0.1"))
      }
      "should show snapshot updates" in {
        u should contain("1.0.1-SNAPSHOT")
      }
    }
    "for stable artifacts" - {
      val u = updates("1.0.0", available)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should show stable updates" in {
        u should contain("1.0.1")
      }
      "should not show snapshot updates" in {
        u should not(contain("1.0.1-SNAPSHOT"))
      }
    }
  }

}
