sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"

crossSbtVersions := Seq("0.13.16", "1.1.0")

enablePlugins(GitVersioning)
git.useGitDescribe := true
git.gitTagToVersionNumber := {
  case VersionNumber(Seq(x, y, z), Seq(), Seq())              => Some(s"$x.$y.$z")
  case VersionNumber(Seq(x, y, z), Seq(since, commit), Seq()) => Some(s"$x.$y.${z + 1}-$since+$commit")
  case _                                                      => None
}

scriptedSbt := Option(System.getenv("SBT_SCRIPTED_VERSION")).getOrElse((sbtVersion in pluginCrossBuild).value)
