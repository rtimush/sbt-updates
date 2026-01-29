import SbtAxis.RichProjectMatrix

ThisBuild / organization := "com.timushev.sbt"
ThisBuild / homepage     := Some(url("https://github.com/rtimush/sbt-updates"))
ThisBuild / licenses += (("BSD 3-Clause", url("https://github.com/rtimush/sbt-updates/blob/master/LICENSE")))
ThisBuild / developers := List(
  Developer("rtimush", "Roman Timushev", "rtimush@gmail.com", url("https://github.com/rtimush"))
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/rtimush/sbt-updates"),
    "scm:git:https://github.com/rtimush/sbt-updates.git",
    Some("scm:git:git@github.com:rtimush/sbt-updates.git")
  )
)
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / pomIncludeRepository             := { _ => false }
ThisBuild / sbtPluginPublishLegacyMavenStyle := false
ThisBuild / publishMavenStyle                := true
ThisBuild / publishTo                        := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots".at(centralSnapshots))
  else localStaging.value
}

ThisBuild / scalacOptions := {
  val base = Seq("-deprecation", "-unchecked", "-feature")
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, minor)) if minor >= 12 => base ++ Seq("-Xsource:3")
    case _                                => base
  }
}

ThisBuild / (pluginCrossBuild / sbtVersion) := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.11.7"
    case "3"    => "2.0.0-RC7"
    case _      => sbtVersion.value
  }
}

lazy val `sbt-1.x`    = SbtAxis("1.x", "1.1.5")
lazy val `sbt-latest` = SbtAxis()
lazy val `sbt-1.0.0`  = SbtAxis("1.0.0")
lazy val `sbt-2.0`    = SbtAxis("2.0", "2.0.0-RC7")


lazy val `sbt-updates` = (projectMatrix in file("."))
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test")
  .sbtPluginRow(`sbt-1.x`)
  .sbtScriptedRow(`sbt-1.0.0`, `sbt-1.x`)
  .sbtScriptedRow(`sbt-latest`, `sbt-1.x`)
  .sbtPluginRow(`sbt-2.0`)
  .sbtScriptedRow(`sbt-2.0`, `sbt-2.0`)

lazy val root = (project in file("."))
  .withId("sbt-updates")
  .aggregate(`sbt-updates`.projectRefs: _*)
  .settings(
    publish / skip               := true,
    compile / skip               := true,
    scalafmtAll / aggregate      := false,
    scalafmtSbt / aggregate      := false,
    scalafmtCheckAll / aggregate := false,
    scalafmtSbtCheck / aggregate := false
  )

Global / scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"
