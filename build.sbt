import SbtAxis.RichProjectMatrix
import com.rallyhealth.sbt.versioning.SnapshotVersion

ThisBuild / name := "sbt-updates"
ThisBuild / organization := "com.timushev.sbt"
ThisBuild / isSnapshot := (ThisBuild / versionFromGit).value.isInstanceOf[SnapshotVersion]
ThisBuild / version := (ThisBuild / version).value.replaceAll("""-SNAPSHOT$""", "")
ThisBuild / homepage := Some(url("https://github.com/rtimush/sbt-updates"))
ThisBuild / licenses += (("BSD 3-Clause", url("https://github.com/rtimush/sbt-updates/blob/master/LICENSE")))

ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

lazy val `sbt-1.x`    = SbtAxis("1.x", "1.1.5")
lazy val `sbt-1.3.10` = SbtAxis("1.3.10")
lazy val `sbt-1.0.0`  = SbtAxis("1.0.0")

lazy val `sbt-0.13.x`  = SbtAxis("0.13.x", "0.13.16")
lazy val `sbt-0.13.16` = SbtAxis("0.13.16")
lazy val `sbt-0.13.9`  = SbtAxis("0.13.9")

lazy val publishSettings = Seq(
  publishMavenStyle := false,
  bintrayRepository := (if (isSnapshot.value) "sbt-plugin-snapshots" else "sbt-plugins"),
  bintrayOrganization in bintray := None,
  bintrayReleaseOnPublish := isSnapshot.value
)

lazy val `sbt-updates` = (projectMatrix in file("."))
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test")
  .sbtPluginRow(`sbt-1.x`, publishSettings: _*)
  .sbtScriptedRow(`sbt-1.0.0`, `sbt-1.x`)
  .sbtScriptedRow(`sbt-1.3.10`, `sbt-1.x`)
  .sbtPluginRow(`sbt-0.13.x`, publishSettings: _*)
  .sbtScriptedRow(`sbt-0.13.9`, `sbt-0.13.x`)
  .sbtScriptedRow(`sbt-0.13.16`, `sbt-0.13.x`)

lazy val root = (project in file("."))
  .withId("sbt-updates")
  .aggregate(`sbt-updates`.projectRefs: _*)
  .settings(
    publish / skip := true,
    compile / skip := true,
    scalafmtAll / aggregate := false,
    scalafmtSbt / aggregate := false,
    scalafmtCheckAll / aggregate := false,
    scalafmtSbtCheck / aggregate := false
  )

Global / scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"
