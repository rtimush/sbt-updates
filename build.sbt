sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"
isSnapshot := versionFromGit.value.isInstanceOf[com.rallyhealth.sbt.versioning.SnapshotVersion]
version := version.value.replaceAll("-SNAPSHOT$", "")
homepage := Some(url("https://github.com/rtimush/sbt-updates"))

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.1" % "test"
)

crossSbtVersions := Seq("1.1.5", "0.13.16")

enablePlugins(ScriptedPlugin)
Global / scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"
scriptedDependencies := Def.taskDyn {
  if (insideCI.value) Def.task(())
  else Def.task(()).dependsOn(Test / compile, publishLocal)
}.value
