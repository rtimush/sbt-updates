proguardSettings

ProguardKeys.options in Proguard ++= Seq(
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

ProguardKeys.inputs in Proguard ++= {
  val dcp = (dependencyClasspath in Embedded).value
  val si = scalaInstance.value
	dcp.files filterNot (_ == si.libraryJar)
}
