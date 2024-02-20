import SbtAxis.RichProjectMatrix

ThisBuild / organization := "com.timushev.sbt"
ThisBuild / homepage     := Some(url("https://github.com/rtimush/sbt-updates"))
ThisBuild / licenses += (("BSD 3-Clause", url("https://github.com/rtimush/sbt-updates/blob/master/LICENSE")))
ThisBuild / publishTo := sonatypePublishToBundle.value
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

sonatypeProfileName := "com.timushev"

ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

lazy val `sbt-1.x`    = SbtAxis("1.x", "1.1.5")
lazy val `sbt-latest` = SbtAxis()
lazy val `sbt-1.0.0`  = SbtAxis("1.0.0")

lazy val `sbt-0.13.x`  = SbtAxis("0.13.x", "0.13.16")
lazy val `sbt-0.13.16` = SbtAxis("0.13.16")
lazy val `sbt-0.13.9`  = SbtAxis("0.13.9")

lazy val `sbt-updates` = (projectMatrix in file("."))
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test")
  .sbtPluginRow(`sbt-1.x`)
  .sbtScriptedRow(`sbt-1.0.0`, `sbt-1.x`)
  .sbtScriptedRow(`sbt-latest`, `sbt-1.x`)
  .sbtPluginRow(`sbt-0.13.x`)
  .sbtScriptedRow(`sbt-0.13.9`, `sbt-0.13.x`)
  .sbtScriptedRow(`sbt-0.13.16`, `sbt-0.13.x`)

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
