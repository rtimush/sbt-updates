package com.timushev.sbt.updates.upgrade

import org.scalatest.LoneElement
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class UpgradePlannerSpec extends AnyFreeSpec with Matchers with LoneElement {

  private val reload4j = Upgrade("ch.qos.reload4j", "reload4j", "1.2.19", "1.2.26")
  private val slf4j    = Upgrade("org.slf4j", "slf4j-api", "1.7.30", "2.0.17")

  private def source(name: String, text: String) = SourceFile(new File(name), text)

  /** Applies the plan and returns the resulting sources, by file name. */
  private def upgrade(upgrades: Seq[Upgrade], sources: SourceFile*): (Map[String, String], UpgradePlan) =
    upgradeWith(scala3First = false, upgrades, sources: _*)

  private def upgradeWith(
      scala3First: Boolean,
      upgrades: Seq[Upgrade],
      sources: SourceFile*
  ): (Map[String, String], UpgradePlan) = {
    val plan    = UpgradePlanner.plan(upgrades, sources, scala3First)
    val results = sources.map { s =>
      val edits = plan.edits.filter(_.source == s).sortBy(-_.literal.contentStart)
      s.file.getName -> edits.foldLeft(s.text) { (text, e) =>
        text.substring(0, e.literal.contentStart) + e.replacement + text.substring(e.literal.contentEnd)
      }
    }
    (results.toMap, plan)
  }

  private def upgradeOne(upgrades: Seq[Upgrade], text: String): (String, UpgradePlan) = {
    val (results, plan) = upgrade(upgrades, source("build.sbt", text))
    (results("build.sbt"), plan)
  }

  "An upgrade planner" - {
    "should upgrade" - {
      "an inline version" in {
        val (result, plan) =
          upgradeOne(Seq(reload4j), """libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19"""")
        result shouldBe """libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.26""""
        plan.skipped shouldBe empty
        plan.notFound shouldBe empty
      }
      "a declaration split across lines and comments" in {
        val (result, _) = upgradeOne(
          Seq(reload4j),
          """libraryDependencies +=
            |  "ch.qos.reload4j" % // the logger
            |    "reload4j" /* pinned */ %
            |      "1.2.19" % Test""".stripMargin
        )
        result should include("\"1.2.26\" % Test")
      }
      "a version defined in the same file" in {
        val (result, _) = upgradeOne(
          Seq(reload4j),
          """val reload4jVersion: String = "1.2.19"
            |libraryDependencies += "ch.qos.reload4j" % "reload4j" % reload4jVersion""".stripMargin
        )
        result should startWith("val reload4jVersion: String = \"1.2.26\"")
        result should endWith("% reload4jVersion")
      }
      "a version defined in another file" in {
        val (results, _) = upgrade(
          Seq(reload4j),
          source(
            "build.sbt",
            """libraryDependencies += "ch.qos.reload4j" % "reload4j" % Dependencies.reload4jVersion"""
          ),
          source(
            "Dependencies.scala",
            """object Dependencies {
              |  object Other { val reload4jVersion = "1.0.0" }
              |  val reload4jVersion = "1.2.19"
              |}""".stripMargin
          )
        )
        results("Dependencies.scala") should include("""val reload4jVersion = "1.0.0"""")
        results("Dependencies.scala") should include("""val reload4jVersion = "1.2.26"""")
      }
      "a version defined through an alias" in {
        val (results, _) = upgrade(
          Seq(reload4j),
          source(
            "build.sbt",
            "import Dependencies._\nlibraryDependencies += \"ch.qos.reload4j\" % \"reload4j\" % logging"
          ),
          source("Dependencies.scala", "object Dependencies {\n  lazy val logging = Versions.reload4j\n}"),
          source("Versions.scala", "object Versions {\n  final val reload4j = \"1.2.19\"\n}")
        )
        results("Versions.scala") should include("\"1.2.26\"")
      }
      "a version shared by dependencies upgraded to the same version" in {
        val core           = Upgrade("org.example", "core", "1.0.0", "1.1.0")
        val util           = Upgrade("org.example", "util", "1.0.0", "1.1.0")
        val (result, plan) = upgradeOne(
          Seq(core, util),
          """val exampleVersion = "1.0.0"
            |libraryDependencies ++= Seq(
            |  "org.example" %% "core" % exampleVersion,
            |  "org.example" %% "util" % exampleVersion
            |)""".stripMargin
        )
        result should startWith("val exampleVersion = \"1.1.0\"")
        plan.edits.flatMap(_.upgrades) should contain theSameElementsAs Seq(core, util)
      }
      "a plugin declaration" in {
        val plugin      = Upgrade("com.example", "sbt-example", "0.1.0", "0.2.0")
        val (result, _) = upgradeOne(Seq(plugin), """addSbtPlugin("com.example" % "sbt-example" % "0.1.0")""")
        result shouldBe """addSbtPlugin("com.example" % "sbt-example" % "0.2.0")"""
      }
    }

    "should not change" - {
      "a version shared with a dependency that is not upgraded" in {
        val text = """val v = "1.7.30"
                     |libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % v, "org.slf4j" % "slf4j-ext" % v)""".stripMargin
        val (result, plan) = upgradeOne(Seq(slf4j), text)
        result shouldBe text
        plan.skipped.head should include("not all of which can be upgraded")
      }
      "a version shared by dependencies with different latest versions" in {
        val ext  = Upgrade("org.slf4j", "slf4j-ext", "1.7.30", "1.7.36")
        val text = """val v = "1.7.30"
                     |libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % v, "org.slf4j" % "slf4j-ext" % v)""".stripMargin
        val (result, plan) = upgradeOne(Seq(slf4j, ext), text)
        result shouldBe text
        plan.skipped.head should include("different latest versions")
      }
      "a declaration shared by two projects that see different latest versions" in {
        // e.g. common settings used by projects on different Scala versions
        val text =
          """lazy val commonSettings = Seq(libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0")
            |lazy val a = project.settings(commonSettings, scalaVersion := "2.13.16")
            |lazy val b = project.settings(commonSettings, scalaVersion := "2.12.20")
            |""".stripMargin
        val fromA = Upgrade("org.typelevel", "cats-core", "2.9.0", "2.13.0")
        val fromB = Upgrade("org.typelevel", "cats-core", "2.9.0", "2.12.0")

        // the outcome must not depend on the order the projects are reported in
        for (upgrades <- Seq(Seq(fromA, fromB), Seq(fromB, fromA))) {
          val (result, plan) = upgradeOne(upgrades, text)
          result shouldBe text
          plan.edits shouldBe empty
          plan.skipped.loneElement should include("different latest versions: ")
          plan.notFound shouldBe empty
        }
      }
      "an ambiguous reference" in {
        val (results, plan) = upgrade(
          Seq(reload4j),
          source("build.sbt", """libraryDependencies += "ch.qos.reload4j" % "reload4j" % reload4jVersion"""),
          source("A.scala", """object A { val reload4jVersion = "1.2.19" }"""),
          source("B.scala", """object B { val reload4jVersion = "1.2.19" }""")
        )
        results("A.scala") should include("1.2.19")
        results("B.scala") should include("1.2.19")
        plan.skipped.head should include("ambiguous")
      }
      "a computed version" in {
        val text = """val v = "1.2." + "19"
                     |libraryDependencies += "ch.qos.reload4j" % "reload4j" % v""".stripMargin
        val (result, plan) = upgradeOne(Seq(reload4j), text)
        result shouldBe text
        plan.skipped should have size 1
      }
      "an interpolated version" in {
        val text = """val patch = 19
                     |libraryDependencies += "ch.qos.reload4j" % "reload4j" % s"1.2.$patch"""".stripMargin
        val (result, plan) = upgradeOne(Seq(reload4j), text)
        result shouldBe text
        plan.skipped should have size 1
      }
      "a literal that does not hold the current version" in {
        val text           = """libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.18""""
        val (result, plan) = upgradeOne(Seq(reload4j), text)
        result shouldBe text
        plan.notFound shouldBe Seq(reload4j)
      }
      "commented out declarations and string contents" in {
        val text =
          """// libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19"
            |/* libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19" /* nested */ "1.2.19" */
            |val doc = "\"ch.qos.reload4j\" % \"reload4j\" % \"1.2.19\""
            |""".stripMargin + "val raw = s" + "\"\"\"" + """"ch.qos.reload4j" % "reload4j" % "1.2.19" ${"x"}""" + "\"\"\""
        val (result, plan) = upgradeOne(Seq(reload4j), text)
        result shouldBe text
        plan.notFound shouldBe Seq(reload4j)
      }
    }

    "should understand Scala 3 syntax" - {
      val braceless = source(
        "Dependencies.scala",
        """object Dependencies:
          |  object Versions:
          |    val reload4j: String = "1.2.19"
          |
          |  val logging = Versions.reload4j
          |""".stripMargin
      )
      val build =
        source("build.sbt", "libraryDependencies += \"ch.qos.reload4j\" % \"reload4j\" % Dependencies.logging\n")

      for (scala3First <- Seq(true, false))
        s"in braceless objects, trying the Scala ${if (scala3First) 3 else 2} dialect first" in {
          val (results, plan) = upgradeWith(scala3First, Seq(reload4j), build, braceless)
          results("Dependencies.scala") should include("val reload4j: String = \"1.2.26\"")
          plan.errors shouldBe empty
        }

      "in top-level definitions of a package" in {
        val (results, _) = upgradeWith(
          scala3First = true,
          Seq(reload4j),
          source("build.sbt", "libraryDependencies += \"ch.qos.reload4j\" % \"reload4j\" % versions.reload4j\n"),
          source("versions.scala", "package versions\n\nval reload4j = \"1.2.19\"\n")
        )
        results("versions.scala") should include("\"1.2.26\"")
      }

      // `if ... then` is Scala 3 only, so the Scala 2 sbt dialect cannot parse this file
      "in sbt 2 build files" in {
        val text =
          """val reload4jVersion = "1.2.19"
            |val useReload4j = true
            |
            |lazy val root = (project in file("."))
            |  .settings(
            |    libraryDependencies ++= (
            |      if useReload4j then Seq("ch.qos.reload4j" % "reload4j" % reload4jVersion)
            |      else Nil
            |    )
            |  )
            |""".stripMargin
        val (results, _) = upgradeWith(scala3First = true, Seq(reload4j), source("build.sbt", text))
        results("build.sbt") should startWith("val reload4jVersion = \"1.2.26\"")
      }
    }

    "should change nothing when a build source cannot be parsed" in {
      val (results, plan) = upgrade(
        Seq(reload4j),
        source("build.sbt", "libraryDependencies += \"ch.qos.reload4j\" % \"reload4j\" % \"1.2.19\"\n"),
        source("Broken.scala", "object Broken {\n  val x = \n")
      )
      results("build.sbt") should include("1.2.19")
      plan.edits shouldBe empty
      plan.errors.head should startWith("Broken.scala:")
    }
  }
}
