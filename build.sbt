sbtPlugin := true

name := "sbt-updates"

organization := "com.timushev.sbt"

version := "0.1.6-SNAPSHOT"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val sbtV = (sbtVersion in sbtPlugin).value
  val scalazV = scalazVersion(sbtV)
  Seq(
    "org.scalaz"    %% "scalaz-concurrent" % scalazV       % "embedded",
    "org.scalatest" %% "scalatest"         % "2.1.4-SNAP1" % "test"
  )
}

// need to work around sbt-cross-building
unmanagedSourceDirectories in Compile += sbtDependentSrcs(
  sbtVersion = (sbtVersion in sbtPlugin).value,
  srcMainDir = (sourceDirectory in Compile).value
)

dependencyClasspath in Compile ++= (dependencyClasspath in Embedded).value

dependencyClasspath in Test ++= (dependencyClasspath in Embedded).value

packagedArtifact in (Compile, packageBin) <<= (packagedArtifact in (Compile, packageBin), (ProguardKeys.proguard in Proguard)) map {
  case ((art, _), jars) => (art, jars.head)
}
