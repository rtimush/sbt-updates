import com.rallyhealth.sbt.versioning.SnapshotVersion

sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"
isSnapshot := versionFromGit.value.isInstanceOf[SnapshotVersion]
version := version.value.replaceAll("\\-SNAPSHOT$", "")
homepage := Some(url("https://github.com/rtimush/sbt-updates"))

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.1" % "test"
)

crossSbtVersions := Seq("0.13.16", "1.1.5")
scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"
scriptedSbt := Option(System.getenv("SBT_SCRIPTED_VERSION")).getOrElse((sbtVersion in pluginCrossBuild).value)
scriptedDependencies := Def.taskDyn {
  if (insideCI.value) Def.task(())
  else Def.task(Def.task(()).dependsOn(compile in Test, publishLocal).value)
}.value
