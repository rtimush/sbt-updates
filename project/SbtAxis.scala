import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt.VirtualAxis.PlatformAxis
import sbt._
import sbt.internal.ProjectMatrix

case class SbtAxis(fullVersion: Option[String], idSuffix: String, directorySuffix: String)
    extends VirtualAxis.WeakAxis {
  val scalaVersion: String =
    fullVersion.map(VersionNumber(_)) match {
      case Some(VersionNumber(Seq(0, 13, _*), _, _))    => "2.10.7"
      case Some(VersionNumber(Seq(1, _*), _, _)) | None => "2.12.10"
      case _                                            => sys.error(s"Unsupported sbt version: $fullVersion")
    }
}

object SbtAxis {

  def apply(): SbtAxis =
    SbtAxis(None, "-latest", "-latest")
  def apply(version: String): SbtAxis =
    SbtAxis(version, version)
  def apply(version: String, fullVersion: String): SbtAxis =
    SbtAxis(Some(fullVersion), "-" + version.replace('.', '_'), "-" + version)

  private val jvm: PlatformAxis = PlatformAxis("jvm", "", "jvm")

  implicit class RichProjectMatrix(val matrix: ProjectMatrix) extends AnyVal {
    def sbtPluginRow(axis: SbtAxis, ss: Def.SettingsDefinition*): ProjectMatrix =
      matrix.customRow(
        autoScalaLibrary = false,
        axisValues = Seq(axis, jvm),
        _.settings(
          sbtPlugin                     := true,
          scalaVersion                  := axis.scalaVersion,
          crossPaths                    := true,
          pluginCrossBuild / sbtVersion := axis.fullVersion.getOrElse(sbtVersion.value)
        ).settings(ss: _*)
      )
    def sbtScriptedRow(axis: SbtAxis, buildAxis: SbtAxis): ProjectMatrix =
      matrix.customRow(
        autoScalaLibrary = false,
        axisValues = Seq(axis, jvm),
        _.enablePlugins(ScriptedPlugin).settings(
          sbtPlugin                     := true,
          scalaVersion                  := axis.scalaVersion,
          crossPaths                    := true,
          pluginCrossBuild / sbtVersion := axis.fullVersion.getOrElse(sbtVersion.value),
          publish / skip                := true,
          compile / skip                := true,
          scriptedDependencies := Def.taskDyn {
            if (insideCI.value) Def.task(())
            else Def.task(()).dependsOn(matrix.finder(buildAxis)(false) / publishLocal)
          }.value
        )
      )
  }

}
