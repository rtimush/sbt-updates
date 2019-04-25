import com.rallyhealth.sbt.versioning.SnapshotVersion

sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"
isSnapshot := versionFromGit.value.isInstanceOf[SnapshotVersion]
version := version.value.replaceAll("\\-SNAPSHOT$", "")

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"

crossSbtVersions := Seq("0.13.16", "1.1.5")

scriptedSbt := Option(System.getenv("SBT_SCRIPTED_VERSION")).getOrElse((sbtVersion in pluginCrossBuild).value)
