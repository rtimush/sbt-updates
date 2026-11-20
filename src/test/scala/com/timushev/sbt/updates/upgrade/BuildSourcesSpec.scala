package com.timushev.sbt.updates.upgrade

import org.scalatest.LoneElement
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class BuildSourcesSpec extends AnyFreeSpec with Matchers with LoneElement {

  private def source(name: String, text: String) = SourceFile(new File(name), text)

  private def analyse(name: String, text: String, scala3First: Boolean = false): BuildSources =
    BuildSources(Seq(source(name, text)), scala3First)

  private def sbt(text: String): BuildSources                           = analyse("build.sbt", text)
  private def scala(text: String): BuildSources                         = analyse("Dependencies.scala", text)
  private def versions(build: BuildSources)                             = build.dependencies.map(_.version)
  private def definition(build: BuildSources, name: String): Definition =
    build.definitions.find(_.name == name).getOrElse(fail(s"no definition of $name in ${build.definitions}"))

  /** The text a literal points at, so offsets are checked against the source rather than hard-coded. */
  private def contentOf(text: String, version: VersionExpr): String =
    version match {
      case VersionExpr.Literal(l) => text.substring(l.contentStart, l.contentEnd)
      case other                  => fail(s"not a string literal: $other")
    }

  "BuildSources" - {
    "should find dependency declarations" - {
      "with each of the %, %% and %%% operators" in {
        val build = sbt(
          """libraryDependencies ++= Seq(
            |  "ch.qos.reload4j" % "reload4j" % "1.2.19",
            |  "org.typelevel" %% "cats-core" % "2.9.0",
            |  "org.scala-js" %%% "scalajs-dom" % "2.8.0"
            |)""".stripMargin
        )

        build.dependencies.map(d => (d.organization, d.name)) shouldBe Seq(
          "ch.qos.reload4j" -> "reload4j",
          "org.typelevel"   -> "cats-core",
          "org.scala-js"    -> "scalajs-dom"
        )

        build.errors shouldBe empty
      }

      "pointing at the version literal between its quotes" in {
        val text  = """libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19""""
        val build = sbt(text)
        val dep   = build.dependencies.loneElement

        contentOf(text, dep.version) shouldBe "1.2.19"
        text.substring(dep.offset) should startWith("\"ch.qos.reload4j\"")

        dep.version match {
          case VersionExpr.Literal(l) =>
            l.content shouldBe "1.2.19"
            text.charAt(l.start) shouldBe '"'

          case other =>
            fail(s"not a literal: $other")
        }
      }

      "in a triple-quoted version" in {
        val text = "libraryDependencies += \"ch.qos.reload4j\" % \"reload4j\" % \"\"\"1.2.19\"\"\""

        contentOf(text, sbt(text).dependencies.loneElement.version) shouldBe "1.2.19"
      }

      "split across lines and comments" in {
        val text =
          """libraryDependencies +=
            |  "ch.qos.reload4j" % // the logger
            |    "reload4j" /* pinned */ %
            |      "1.2.19" % Test""".stripMargin

        contentOf(text, sbt(text).dependencies.loneElement.version) shouldBe "1.2.19"
      }

      "followed by a configuration, without mistaking the configuration for a version" in {
        val text    = """libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19" % "test""""
        val build   = sbt(text)
        val element = build.dependencies.loneElement

        element.name shouldBe "reload4j"
        contentOf(text, element.version) shouldBe "1.2.19"
      }

      "inside other expressions, e.g. addSbtPlugin" in {
        val build = sbt("""addSbtPlugin("com.example" % "sbt-example" % "0.1.0")""")

        build.dependencies.loneElement.name shouldBe "sbt-example"
      }

      "with a version referencing a definition" in {
        val ref = VersionExpr.Reference(List("reload4jVersion"))
        versions(
          sbt("""libraryDependencies += "ch.qos.reload4j" % "reload4j" % reload4jVersion""")
        ) shouldBe Seq(ref)

        val path = VersionExpr.Reference(List("Deps", "Versions", "reload4j"))
        versions(
          sbt("""libraryDependencies += "ch.qos.reload4j" % "reload4j" % Deps.Versions.reload4j""")
        ) shouldBe Seq(path)
      }

      // without types, `a.b` is either a qualified name or a call of a parameterless method; the planner then finds
      // no definition `trim` in an object `reload4jVersion`, and skips the dependency
      "with a parameterless method call on a version, which reads as a qualified reference" in {
        versions(sbt("""libraryDependencies += "ch.qos.reload4j" % "reload4j" % reload4jVersion.trim""")) shouldBe
          Seq(VersionExpr.Reference(List("reload4jVersion", "trim")))
      }

      "with a version that cannot be edited" in {
        val unsupported = Seq(
          "(\"1.2.\" + \"19\")",    // concatenation
          "s\"1.2.$patch\"",        // interpolation
          "reload4jVersion.trim()", // method call
          "versionOf(\"x\")",       // application
          "\"1.2.19\\t\""           // escape sequence
        )

        unsupported.foreach { version =>
          withClue(version) {
            versions(sbt(s"""libraryDependencies += "ch.qos.reload4j" % "reload4j" % $version""")) shouldBe Seq(
              VersionExpr.Unsupported
            )
          }
        }
      }
    }

    "should not find dependency declarations" - {
      "in comments" in {
        val build = sbt(
          """// libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19"
            |/* libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19" /* nested */ */
            |""".stripMargin
        )

        build.dependencies shouldBe empty
      }

      "in string contents" in {
        sbt(
          "val doc = \"\\\"ch.qos.reload4j\\\" % \\\"reload4j\\\" % \\\"1.2.19\\\"\"\n" +
            "val raw = \"\"\"\"ch.qos.reload4j\" % \"reload4j\" % \"1.2.19\"\"\"\"\n"
        ).dependencies shouldBe empty
      }

      "without a version, or with an organization that is not a literal" in {
        sbt(
          """val module = "ch.qos.reload4j" % "reload4j"
            |val other  = organization % "reload4j" % "1.2.19"
            |""".stripMargin
        ).dependencies shouldBe empty
      }
    }

    "should find definitions" - {
      "of vals, lazy vals and parameterless defs, with or without a type" in {
        val text =
          """object Dependencies {
            |  val a = "1.0.0"
            |  lazy val b = "2.0.0"
            |  final val c: String = "3.0.0"
            |  def d = "4.0.0"
            |  def e: String = "5.0.0"
            |}""".stripMargin
        val build = scala(text)

        build.definitions.map(_.name) shouldBe Seq("a", "b", "c", "d", "e")
        build.definitions.map(d => contentOf(text, d.rhs)) shouldBe
          Seq("1.0.0", "2.0.0", "3.0.0", "4.0.0", "5.0.0")
      }

      "with a reference or an unsupported value" in {
        val build = scala(
          """object Dependencies {
            |  val alias = Versions.reload4j
            |  val computed = "1.2." + "19"
            |}""".stripMargin
        )
        definition(build, "alias").rhs shouldBe VersionExpr.Reference(List("Versions", "reload4j"))
        definition(build, "computed").rhs shouldBe VersionExpr.Unsupported
      }

      "but not of defs with parameters or vals bound by a pattern" in {
        scala(
          """object Dependencies {
            |  def versionOf(name: String) = "1.0.0"
            |  def generic[A] = "1.0.0"
            |  val (major, minor) = ("1", "0")
            |}""".stripMargin
        ).definitions shouldBe empty
      }
    }

    "should record the enclosing scope" - {
      "of nested packages, objects, classes and traits" in {
        val build = scala(
          """package com.example
            |
            |object Dependencies {
            |  object Versions {
            |    val reload4j = "1.2.19"
            |  }
            |  class Holder { val inClass = "1.0.0" }
            |  trait Mixin { val inTrait = "1.0.0" }
            |  val deps = Seq("ch.qos.reload4j" % "reload4j" % Versions.reload4j)
            |}""".stripMargin
        )
        definition(build, "reload4j").scope shouldBe List("com", "example", "Dependencies", "Versions")
        definition(build, "inClass").scope shouldBe List("com", "example", "Dependencies", "Holder")
        definition(build, "inTrait").scope shouldBe List("com", "example", "Dependencies", "Mixin")
        build.dependencies.loneElement.scope shouldBe List("com", "example", "Dependencies")
      }

      "of package objects" in {
        val build = scala("package com\n\npackage object example {\n  val reload4j = \"1.2.19\"\n}\n")

        definition(build, "reload4j").scope shouldBe List("com", "example")
      }

      "as empty at the top level of an .sbt file" in {
        val build = sbt("val reload4jVersion = \"1.2.19\"\nlibraryDependencies += \"a\" % \"b\" % reload4jVersion\n")

        definition(build, "reload4jVersion").scope shouldBe Nil
        build.dependencies.loneElement.scope shouldBe Nil
      }
    }

    "should combine all sources" in {
      val build   = source("build.sbt", "libraryDependencies += \"a\" % \"b\" % Dependencies.v\n")
      val deps    = source("project/Dependencies.scala", "object Dependencies {\n  val v = \"1.0.0\"\n}\n")
      val sources = BuildSources(Seq(build, deps), scala3First = false)

      sources.dependencies.loneElement.source shouldBe build
      definition(sources, "v").source shouldBe deps
      sources.errors shouldBe empty
    }

    "should parse each file with whichever dialect accepts it" - {
      // `if ... then` is Scala 3 only, procedure syntax is Scala 2 only
      val scala3Sbt =
        """val reload4jVersion = "1.2.19"
          |libraryDependencies ++= (
          |  if (true) then Seq("ch.qos.reload4j" % "reload4j" % reload4jVersion) else Nil
          |)""".stripMargin
      val scala3Braceless =
        """object Dependencies:
          |  object Versions:
          |    val reload4j = "1.2.19"
          |""".stripMargin
      val scala2Only =
        """object Dependencies {
          |  def run() { println("x") }
          |  val reload4j = "1.2.19"
          |}""".stripMargin

      for (scala3First <- Seq(true, false)) {
        val order = if (scala3First) "Scala 3 first" else "Scala 2 first"

        s"Scala 3 build files, trying $order" in {
          val build = analyse("build.sbt", scala3Sbt, scala3First)

          build.errors shouldBe empty
          build.dependencies.loneElement.name shouldBe "reload4j"
        }

        s"Scala 3 braceless sources, trying $order" in {
          val build = analyse("Dependencies.scala", scala3Braceless, scala3First)

          build.errors shouldBe empty
          definition(build, "reload4j").scope shouldBe List("Dependencies", "Versions")
        }

        s"Scala 2 only sources, trying $order" in {
          val build = analyse("Dependencies.scala", scala2Only, scala3First)

          build.errors shouldBe empty
          definition(build, "reload4j").scope shouldBe List("Dependencies")
        }
      }
    }

    "should report a file that no dialect can parse" - {
      val broken = SourceFile(
        file = new File("/abs/project/Broken.scala"),
        text = """object Broken {
                  |  val x =
                  |""".stripMargin,
        path = "project/Broken.scala"
      )
      val valid = source("build.sbt", "libraryDependencies += \"a\" % \"dep\" % \"1.0.0\"\n")

      "with its display path and line, once" in {
        val build = BuildSources(Seq(broken), scala3First = false)

        build.errors.loneElement should startWith("project/Broken.scala:3: ")
        build.dependencies shouldBe empty
        build.definitions shouldBe empty
      }

      "while still analysing the other files" in {
        val build = BuildSources(Seq(valid, broken), scala3First = true)

        build.errors should have size 1
        build.dependencies.loneElement.name shouldBe "dep"
      }
    }
  }
}
