sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.6" % "test"
)

scriptedSettings

enablePlugins(GitVersioning)
git.useGitDescribe := true
