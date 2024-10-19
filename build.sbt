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

ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

lazy val `sbt-2` = SbtAxis("2.x", "2.0.0-M2")

lazy val `sbt-1.x`    = SbtAxis("1.x", "1.1.5")
lazy val `sbt-latest` = SbtAxis()

lazy val `sbt-updates` = (projectMatrix in file("."))
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test")
  .sbtPluginRow(`sbt-1.x`)
  .sbtScriptedRow(`sbt-latest`, `sbt-1.x`)
  .sbtPluginRow(`sbt-2`)

lazy val `sbt-updates-2_x` = `sbt-updates`
  .finder(`sbt-2`)(false)
  .settings(
    scripted := {
      // TODO enable scripted test
      (Test / test).value
    }
  )

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
