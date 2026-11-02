import com.timushev.sbt.updates.UpdatesKeys._
import sbt.Keys._
import sbt._

// kept out of build.sbt, so that the assertions do not match their own source code
object Check {
  private val modules = Set("reload4j", "slf4j-api", "gson", "commons-text")

  val settings: Seq[Setting[_]] = Seq(
    InputKey[Unit]("check") := {
      val data     = dependencyUpdatesData.all(ScopeFilter(inAggregates(ThisProject))).value
      val outdated = data.flatMap(_.keys).filter(m => modules(m.name))
      expect(outdated.isEmpty, s"Dependencies were not upgraded: ${outdated.mkString(", ")}")

      val upgraded = (libraryDependencies.value ++ (LocalProject("core") / libraryDependencies).value).filter(m => modules(m.name))
      expect(upgraded.size == modules.size, s"Dependencies are missing: ${upgraded.mkString(", ")}")
      upgraded.foreach { m =>
        expect(m.revision.matches("""\d+(\.\d+)*"""), s"${m.name} was upgraded to ${m.revision}, not a release")
      }

      val build = IO.readLines(baseDirectory.value / "build.sbt")
      val deps  = IO.readLines(baseDirectory.value / "project" / "Dependencies.scala")
      def line(lines: Seq[String], prefix: String) =
        lines.find(_.trim.startsWith(prefix)).getOrElse(sys.error(s"No line starting with $prefix"))

      expect(line(build, "// libraryDependencies").endsWith("% \"1.2.19\""), "a comment was changed")
      expect(!line(build, "val slf4jVersion").contains("1.7.30"), "slf4jVersion was not changed")
      expect(line(build, "val unrelatedVersion").endsWith("\"1.7.30\""), "unrelatedVersion was changed")
      expect(line(build, "\"org.slf4j\"").endsWith("% slf4jVersion,"), "slf4jVersion is no longer referenced")
      expect(line(build, "\"com.google.code.gson\"").endsWith("% Dependencies.gsonVersion"), "gsonVersion is no longer referenced")
      expect(line(build, "libraryDependencies +=").endsWith("% // a multi-line declaration"), "the declaration was reformatted")
      expect(line(deps, "val gsonVersion").endsWith("\"2.8.0\""), "Dependencies.Legacy.gsonVersion was changed")
      expect(line(build, "\"org.typelevel\"").contains("% \"0.17.0\""), "spire was not upgraded to the latest common release")
      expect(line(build, "\"org.scala-lang.modules\"").contains("% \"1.0.0\""), "parallel collections were upgraded")
      expect(!deps.reverse.find(_.trim.startsWith("val gsonVersion")).exists(_.contains("2.8.0")), "gsonVersion was not changed")
    }
  )

  private def expect(condition: Boolean, message: => String): Unit = if (!condition) sys.error(message)
}
