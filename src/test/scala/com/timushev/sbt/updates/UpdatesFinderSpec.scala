package com.timushev.sbt.updates

import org.scalatest.{FreeSpec, Matchers}
import sbt._

import scala.concurrent.Await
import scala.concurrent.duration._

class UpdatesFinderSpec extends FreeSpec with Matchers {

  def updates(current: String, available: Seq[String]): Set[String] =
    Await.result(UpdatesFinder.findUpdates(Seq(new FixedMetadataLoader(available)))(ModuleID("a", "b", current)), 1.minute)
      .map(_.toString())

  val available = Seq(
    "0.9.9-SNAPSHOT", "0.9.9-M3", "0.9.9",
    "1.0.0-SNAPSHOT", "1.0.0-M2", "1.0.0-M3", "1.0.0",
    "1.0.1-SNAPSHOT", "1.0.1-M3", "1.0.1"
  )

  "An updates finder" - {
    "for snapshot artifacts" - {
      val u = updates("1.0.0-SNAPSHOT", available)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
      }
      "should not show old milestones" in {
        u should not(contain("0.9.9-M3"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show current milestones" in {
        u should not(contain("1.0.0-M3"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should show stable updates" in {
        u should (contain("1.0.0") and contain("1.0.1"))
      }
      "should show milestone updates" in {
        u should contain("1.0.1-M3")
      }
      "should show snapshot updates" in {
        u should contain("1.0.1-SNAPSHOT")
      }
    }
    "for milestone artifacts" - {
      val u = updates("1.0.0-M2", available)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show old milestones" in {
        u should not(contain("0.9.9-M3"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should show current milestones" in {
        u should contain("1.0.0-M3")
      }
      "should show stable updates" in {
        u should contain("1.0.1")
      }
      "should not show snapshot updates" in {
        u should not(contain("1.0.1-SNAPSHOT"))
      }
      "should show milestone updates" in {
        u should contain("1.0.1-M3")
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
      "should not show old milestones" in {
        u should not(contain("0.9.9-M3"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should not show current milestones" in {
        u should not(contain("1.0.0-M3"))
      }
      "should show stable updates" in {
        u should contain("1.0.1")
      }
      "should not show snapshot updates" in {
        u should not(contain("1.0.1-SNAPSHOT"))
      }
      "should not show milestone updates" in {
        u should not(contain("1.0.1-M3"))
      }
    }
  }

}
