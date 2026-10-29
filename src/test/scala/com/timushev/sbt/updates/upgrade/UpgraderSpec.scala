package com.timushev.sbt.updates.upgrade

import com.timushev.sbt.updates.upgrade.Upgrader.ProjectUpdates
import com.timushev.sbt.updates.versions.Version
import org.scalatest.LoneElement
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import sbt.{globFilter => _, _}

import scala.collection.immutable.SortedSet

class UpgraderSpec extends AnyFreeSpec with Matchers with LoneElement {

  private val cats  = "org.typelevel"  %% "cats-core" % "2.9.0"
  private val slf4j = "org.slf4j"       % "slf4j-api" % "1.7.30"
  private val r4j   = "ch.qos.reload4j" % "reload4j"  % "1.2.19"

  private def newer(versions: String*): SortedSet[Version] = SortedSet(versions.map(Version(_)): _*)

  /** A project declaring `module`, with the given newer versions; none means it is already up to date. */
  private def project(id: String, module: ModuleID, versions: String*): ProjectUpdates =
    ProjectUpdates(id, Seq(module), if (versions.isEmpty) Map.empty else Map(module -> newer(versions: _*)))

  "Upgrader.latestReleases" - {
    "should pick the latest release" in {
      val (upgrades, conflicts) = Upgrader.latestReleases(Seq(project("a", r4j, "1.2.20", "1.2.27", "1.2.25")))
      upgrades.loneElement shouldBe Upgrade("ch.qos.reload4j", "reload4j", "1.2.19", "1.2.27")
      conflicts shouldBe empty
    }

    "should ignore pre-releases and snapshots" in {
      val (upgrades, _) =
        Upgrader.latestReleases(Seq(project("a", slf4j, "2.0.17", "2.1.0-alpha1", "2.1.0-SNAPSHOT")))
      upgrades.loneElement.target shouldBe "2.0.17"
    }

    "should neither upgrade nor report a dependency with only newer pre-releases" in {
      Upgrader.latestReleases(Seq(project("a", slf4j, "2.1.0-alpha1"))) shouldBe ((Nil, Nil))
    }

    "for a dependency declared by several projects" - {
      "should upgrade it once when they agree" in {
        val (upgrades, conflicts) =
          Upgrader.latestReleases(Seq(project("a", cats, "2.10.0", "2.13.0"), project("b", cats, "2.10.0", "2.13.0")))
        upgrades.loneElement.target shouldBe "2.13.0"
        conflicts shouldBe empty
      }

      "should pick the latest release available to all of them" in {
        val (upgrades, conflicts) = Upgrader.latestReleases(
          Seq(project("a", cats, "2.10.0", "2.12.0", "2.13.0"), project("b", cats, "2.10.0", "2.12.0"))
        )
        upgrades.loneElement.target shouldBe "2.12.0"
        conflicts shouldBe empty
      }

      "should not upgrade it when one of them has no newer version" in {
        val (upgrades, conflicts) = Upgrader.latestReleases(Seq(project("a", cats, "2.13.0"), project("b", cats)))
        upgrades shouldBe empty
        conflicts.loneElement shouldBe
          "org.typelevel:cats-core 2.9.0: no newer release is available to every project using it (a: 2.13.0, b: none)"
      }

      "should not upgrade it when they have no newer release in common" in {
        val (upgrades, conflicts) =
          Upgrader.latestReleases(Seq(project("a", cats, "2.13.0"), project("b", cats, "2.12.0")))
        upgrades shouldBe empty
        conflicts.loneElement should endWith("(a: 2.13.0, b: 2.12.0)")
      }
    }

    "should not be constrained by projects that do not declare the dependency" in {
      val (upgrades, conflicts) = Upgrader.latestReleases(Seq(project("a", cats, "2.13.0"), project("b", r4j)))
      upgrades.loneElement.target shouldBe "2.13.0"
      conflicts shouldBe empty
    }

    "should treat different current versions of a module separately" in {
      val older                 = "org.typelevel" %% "cats-core" % "2.8.0"
      val (upgrades, conflicts) =
        Upgrader.latestReleases(Seq(project("a", cats, "2.13.0"), project("b", older, "2.9.0", "2.13.0")))
      upgrades.map(u => u.current -> u.target) shouldBe Seq("2.8.0" -> "2.13.0", "2.9.0" -> "2.13.0")
      conflicts shouldBe empty
    }
  }
}
