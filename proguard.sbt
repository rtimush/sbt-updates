import ProguardKeys._

proguardSettings

options in Proguard ++= Seq(
    "-dontwarn",
    "-dontnote",
    "-dontoptimize",
    "-keep class com.timushev.sbt.updates.**",
    "-keepclassmembers class ** { *; }",
    "-repackageclasses 'com.timushev.sbt.updates.libs'"
)

ProguardKeys.libraries in Proguard ++= {
  val ccp = (dependencyClasspath in Compile).value
  val rcp = (dependencyClasspath in Embedded).value
  ccp.files filterNot rcp.files.toSet
}

inputs in Proguard ++= {
  val dcp = (dependencyClasspath in Embedded).value
  val si = scalaInstance.value
	dcp.files filterNot (_ == si.libraryJar)
}

lazy val publishMinJar = taskKey[java.io.File]("publish-min-jar")

publishMinJar := (proguard in Proguard).value.head

packagedArtifact in (Compile, packageBin) := {
  val (art, _) = (packagedArtifact in (Compile, packageBin)).value
  val jar = publishMinJar.value
  (art, jar)
}

dependencyClasspath in Compile ++= (dependencyClasspath in Embedded).value

dependencyClasspath in Test ++= (dependencyClasspath in Embedded).value
