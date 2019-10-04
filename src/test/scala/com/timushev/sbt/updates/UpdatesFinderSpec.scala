package com.timushev.sbt.updates

import com.timushev.sbt.updates.metadata.FixedMetadataLoader
import org.scalatest.{FreeSpec, Matchers}
import sbt.{globFilter => _, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class UpdatesFinderSpec extends FreeSpec with Matchers {

  def updates(current: String, available: Seq[String], allowPreRelease: Boolean): Set[String] =
    Await
      .result(UpdatesFinder.findUpdates(Seq(new FixedMetadataLoader(available)), allowPreRelease)(
                ModuleID("a", "b", current)),
              1.minute)
      .map(_.toString())

  val available = Seq(
    "0.9.9-SNAPSHOT",
    "0.9.9-M3",
    "0.9.9",
    "1.0.0-SNAPSHOT",
    "1.0.0-M2",
    "1.0.0-M3",
    "1.0.0",
    "1.0.1-SNAPSHOT",
    "1.0.1-M3",
    "1.0.1",
    "1.1.0"
  )

  "An updates finder" - {
    "for snapshot artifacts" - {
      val u = updates("1.0.0-SNAPSHOT", available, allowPreRelease = false)
      val pu = updates("1.0.0-SNAPSHOT", available, allowPreRelease = true)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
      }
      "should not show old milestones" in {
        u should not(contain("0.9.9-M3"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
      }
      "should show current milestones" in {
        u should contain("1.0.0-M3")
      }
      "should show stable updates" in {
        u should contain("1.0.0").and(contain("1.0.1"))
      }
      "should show milestone updates" in {
        u should contain("1.0.1-M3")
      }
      "should show snapshot updates" in {
        u should contain("1.0.1-SNAPSHOT")
      }
      "should have no differences regarding optional pre-releases" in {
        u should be(pu)
      }
    }
    "for milestone artifacts" - {
      val u = updates("1.0.0-M2", available, allowPreRelease = false)
      val pu = updates("1.0.0-M2", available, allowPreRelease = true)
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
      "should have no differences regarding optional pre-releases" in {
        u should be(pu)
      }
    }
    "for stable artifacts" - {
      val u = updates("1.0.0", available, allowPreRelease = false)
      val pu = updates("1.0.0", available, allowPreRelease = true)
      val uPlus = updates("1.0.+", available, allowPreRelease = false)
      "should not show old stable versions" in {
        u should not(contain("0.9.9"))
        pu should not(contain("0.9.9"))
        uPlus should not(contain("0.9.9"))
      }
      "should not show old snapshots" in {
        u should not(contain("0.9.9-SNAPSHOT"))
        pu should not(contain("0.9.9-SNAPSHOT"))
        uPlus should not(contain("0.9.9-SNAPSHOT"))
      }
      "should not show old milestones" in {
        u should not(contain("0.9.9-M3"))
        pu should not(contain("0.9.9-M3"))
        uPlus should not(contain("0.9.9-M3"))
      }
      "should not show current snapshot" in {
        u should not(contain("1.0.0-SNAPSHOT"))
        pu should not(contain("1.0.0-SNAPSHOT"))
        uPlus should not(contain("1.0.0-SNAPSHOT"))
      }
      "should not show current milestones" in {
        u should not(contain("1.0.0-M3"))
        pu should not(contain("1.0.0-M3"))
        uPlus should not(contain("1.0.0-M3"))
      }
      "should show stable updates" in {
        u should contain("1.0.1")
        u should contain("1.1.0")
        pu should contain("1.0.1")
        pu should contain("1.1.0")
      }
      "should not show snapshot updates" in {
        u should not(contain("1.0.1-SNAPSHOT"))
        pu should not(contain("1.0.1-SNAPSHOT"))
        uPlus should not(contain("1.0.1-SNAPSHOT"))
      }
      "should not show milestone updates" in {
        u should not(contain("1.0.1-M3"))
        uPlus should not(contain("1.0.1-M3"))
      }
      "should show milestone updates when allowing pre-releases" in {
        pu should contain("1.0.1-M3")
      }
      "should show minor updates when using plus patch version" in {
        uPlus should contain("1.1.0")
      }
      "should not show patch updates when using plus patch version" in {
        uPlus should not(contain("1.0.0"))
        uPlus should not(contain("1.0.1"))
      }
    }
  }
}
