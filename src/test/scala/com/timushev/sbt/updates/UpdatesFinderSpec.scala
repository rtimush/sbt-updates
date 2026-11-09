package com.timushev.sbt.updates

import com.timushev.sbt.updates.metadata.{FixedMetadataLoader, MetadataLoader}
import com.timushev.sbt.updates.versions.Version
import sbt.{globFilter => _, _}

import java.io.{FileNotFoundException, IOException}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class UpdatesFinderSpec extends AnyFreeSpec with Matchers {
  def updates(current: String, available: Seq[String], allowPreRelease: Boolean): Set[String] =
    Await
      .result(
        UpdatesFinder
          .findUpdates(Seq(new FixedMetadataLoader(available)), allowPreRelease)(ModuleID("a", "b", current)),
        1.minute
      )
      .versions
      .map(_.text)

  def failing(name: String, cause: Throwable): MetadataLoader =
    new MetadataLoader {
      val repository: String                                  = name
      def getVersions(module: ModuleID): Future[Seq[Version]] = Future.failed(cause)
    }

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
      val u  = updates("1.0.0-SNAPSHOT", available, allowPreRelease = false)
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
      val u  = updates("1.0.0-M2", available, allowPreRelease = false)
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
      val u     = updates("1.0.0", available, allowPreRelease = false)
      val pu    = updates("1.0.0", available, allowPreRelease = true)
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
    "for repositories that cannot be asked" - {
      val module                         = ModuleID("a", "b", "1.0.0")
      def find(loaders: MetadataLoader*) =
        Await.result(UpdatesFinder.findUpdates(loaders, allowPreRelease = false)(module), 1.minute)
      "should report the failure and keep the updates found elsewhere" in {
        val cause  = new IOException("HTTP 503")
        val result = find(new FixedMetadataLoader(Seq("1.0.1")), failing("broken", cause))
        result.versions.map(_.text) shouldBe Set("1.0.1")
        result.failures shouldBe Seq(UpdatesFinder.LookupFailure("broken", module, cause))
      }
      "should not report a repository that doesn't have the module" in {
        find(failing("elsewhere", new FileNotFoundException("missing"))).failures shouldBe empty
      }
      "should report a repository whose lookup fails before it even starts" in {
        val cause  = new IllegalArgumentException("bad pattern")
        val broken = new MetadataLoader {
          val repository: String                                  = "broken"
          def getVersions(module: ModuleID): Future[Seq[Version]] = throw cause
        }
        find(broken).failures shouldBe Seq(UpdatesFinder.LookupFailure("broken", module, cause))
      }
    }
  }
}
